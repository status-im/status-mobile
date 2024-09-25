(ns status-im.contexts.wallet.wallet-connect.utils.typed-data
  (:require [clojure.string :as string]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
            [status-im.contexts.wallet.wallet-connect.utils.rpc :as rpc]
            [utils.number :as number]))

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

(defn- format-flattened-key
  [k]
  (cond
    (keyword? k) (name k)
    (number? k)  (str k)
    (string? k)  k
    :else        "unsupported-key"))

(defn- flatten-map
  [data prefix]
  (reduce-kv (fn [acc k v]
               (->> (format-flattened-key k)
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
            {:label k
             :value v}))
        data))

(defn flatten-typed-data
  [typed-data]
  (-> typed-data
      (select-keys [:domain :message])
      flatten-data
      (format-flat-keys ": ")))

(defn get-chain-id
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

(defn sign
  [password address data chain-id-eip155 version]
  (let [legacy?  (= version :v1)
        chain-id (networks/eip155->chain-id chain-id-eip155)]
    (rpc/wallet-safe-sign-typed-data data
                                     address
                                     password
                                     chain-id
                                     legacy?)))
