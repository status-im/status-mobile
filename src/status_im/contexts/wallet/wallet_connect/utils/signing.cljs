(ns status-im.contexts.wallet.wallet-connect.utils.signing
  (:require [native-module.core :as native-module]
            [promesa.core :as promesa]
            [schema.core :as schema]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as
             data-store]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
            [status-im.contexts.wallet.wallet-connect.utils.rpc :as rpc]
            [utils.hex :as hex]
            [utils.number :as number]
            [utils.transforms :as transforms]))

(defn typed-data-chain-id
  "Returns the `:chain-id` from typed data if it's present and if the EIP712 domain defines it. Without
  the `:chain-id` in the domain type, it will not be signed as part of the typed-data."
  [typed-data]
  (let [chain-id-type? (->> typed-data
                            :types
                            :EIP712Domain
                            (some #(= "chainId" (:name %))))
        data-chain-id  (-> typed-data
                           :domain
                           :chainId
                           number/parse-int)]
    (when (and chain-id-type?
               (not (zero? data-chain-id)))
      data-chain-id)))

(def ?eip712-type
  [:map
   [:name :string]
   [:type :string]])

(def ?eip712-data
  [:map
   [:types
    [:map
     [:EIP712Domain {:optional true} [:vector ?eip712-type]]
     [:malli.core/default [:map-of :keyword [:vector ?eip712-type]]]]]
   [:domain {:optional true} [:map-of :keyword :any]]
   [:primaryType :string]
   [:message map?]])

(schema/=> typed-data-chain-id
  [:=>
   [:cat ?eip712-data]
   [:maybe :int]])

(defn eth-sign
  [password address data]
  (-> {:data     data
       :account  address
       :password password}
      transforms/clj->json
      native-module/sign-message
      (promesa/then data-store/extract-native-call-signature)))

(schema/=> eth-sign
  [:=>
   [:catn
    [:password :string]
    [:address :string]
    [:data :string]]
   :schema.common/promise])

(defn personal-sign
  [password address data]
  (-> (rpc/wallet-hash-message-eip-191 data)
      (promesa/then #(rpc/wallet-sign-message % address password))
      (promesa/then hex/prefix-hex)))

(schema/=> personal-sign
  [:=>
   [:catn
    [:password :string]
    [:address :string]
    [:data :string]]
   :schema.common/promise])

(defn eth-sign-typed-data
  [password address data chain-id-eip155 version]
  (let [legacy?  (= version :v1)
        chain-id (networks/eip155->chain-id chain-id-eip155)]
    (rpc/wallet-safe-sign-typed-data data
                                     address
                                     password
                                     chain-id
                                     legacy?)))

(schema/=> eth-sign-typed-data
  [:=>
   [:catn
    [:password :string]
    [:address :string]
    [:data :string]
    [:chain-id-eip155 :string]
    [:version [:enum :v1 :v4]]]
   :schema.common/promise])
