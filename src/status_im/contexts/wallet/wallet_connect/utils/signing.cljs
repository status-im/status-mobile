(ns status-im.contexts.wallet.wallet-connect.utils.signing
  (:require [native-module.core :as native-module]
            [promesa.core :as promesa]
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
    (when chain-id-type?
      data-chain-id)))

(defn eth-sign
  [password address data]
  (-> {:data     data
       :account  address
       :password password}
      transforms/clj->json
      native-module/sign-message
      (promesa/then data-store/extract-native-call-signature)))

(defn personal-sign
  [password address data]
  (-> (rpc/wallet-hash-message-eip-191 data)
      (promesa/then #(rpc/wallet-sign-message % address password))
      (promesa/then hex/prefix-hex)))

(defn eth-sign-typed-data
  [password address data chain-id-eip155 version]
  (let [legacy?  (= version :v1)
        chain-id (networks/eip155->chain-id chain-id-eip155)]
    (rpc/wallet-safe-sign-typed-data data
                                     address
                                     password
                                     chain-id
                                     legacy?)))
