(ns status-im.contexts.wallet.wallet-connect.utils.signing
  (:require [clojure.string :as string]
            [native-module.core :as native-module]
            [promesa.core :as promesa]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as
             data-store]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
            [status-im.contexts.wallet.wallet-connect.utils.rpc :as rpc]
            [utils.hex :as hex]
            [utils.number :as number]
            [utils.transforms :as transforms]))

(declare flatten-data)

(defn- flatten-vec
  [data prefix]
  (->> data
       (map-indexed vector)
       (reduce (fn [acc [idx v]]
                 (->> (str idx)
                      (conj prefix)
                      (flatten-data v acc)))
               [])))

(defn- format-flatten-prefix
  [k]
  (cond
    (keyword? k) (name k)
    (number? k)  (str k)
    (string? k)  k
    :else        "unsupported-key"))

(defn- flatten-map
  [data prefix]
  (reduce-kv (fn [acc k v]
               (->> (format-flatten-prefix k)
                    (conj prefix)
                    (flatten-data v acc)))
             []
             data))

(defn flatten-data
  ([value]
   (flatten-data value [] []))
  ([value acc prefix]
   (cond
     (map? value)    (into acc (flatten-map value prefix))
     (vector? value) (into acc (flatten-vec value prefix))
     :else           (conj acc [prefix value]))))

(defn format-flat-keys
  [data separator]
  (mapv (fn [[kv v]]
          (let [k (-> separator
                      (string/join kv)
                      (str separator)
                      string/trim)]
            [k v]))
        data))

(defn flatten-typed-data
  [typed-data]
  (-> typed-data
      (select-keys [:domain :message])
      flatten-data
      (format-flat-keys ": ")))

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
