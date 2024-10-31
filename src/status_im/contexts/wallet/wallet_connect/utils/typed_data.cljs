(ns status-im.contexts.wallet.wallet-connect.utils.typed-data
  (:require [clojure.string :as string]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.rpc :as wallet-rpc]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
            [utils.number :as number]))

(declare flatten-data)

(defn- format-flattened-key
  [k]
  (cond
    (keyword? k) (name k)
    (number? k)  (str k)
    (string? k)  k
    :else        "unsupported-key"))

(defn- flatten-map
  [data path]
  (reduce-kv (fn [acc k v]
               (->> (format-flattened-key k)
                    (conj path)
                    (flatten-data v acc)))
             []
             data))

(defn- flatten-vec
  [data path]
  (->> data
       (map-indexed vector)
       (reduce (fn [acc [idx v]]
                 (->> (str idx)
                      (conj path)
                      (flatten-data v acc)))
               [])))

(defn flatten-data
  "Recursively flatten a map or vector into a flat vector.

  e.g. `[[[\"person\" \"first-name\"] \"Rich\"]
        [[[\"person\" \"last-name\"] \"Hickey\"]]]`"
  ([value]
   (flatten-data value [] []))
  ([value acc path]
   (cond
     (map? value)    (into acc (flatten-map value path))
     (vector? value) (into acc (flatten-vec value path))
     :else           (conj acc [path value]))))

(defn format-fields
  "Format the fields into maps with `:label` & `:value`, where the label
  is the flattened keys joined with a separator

  e.g. `{:label \"person: first-name:\" :value \"Rich\"}`"
  [data separator]
  (mapv (fn [[kv v]]
          {:label (-> separator
                      (string/join kv)
                      (str separator)
                      string/trim)
           :value v})
        data))

(defn flatten-typed-data
  "Flatten typed data and prepare it for UI"
  [typed-data]
  (-> typed-data
      (select-keys [:domain :message])
      flatten-data
      (format-fields ": ")))

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
    (wallet-rpc/safe-sign-typed-data data
                                     address
                                     password
                                     chain-id
                                     legacy?)))

(defn typed-data-request?
  [method]
  (contains?
   #{constants/wallet-connect-eth-sign-typed-v4-method
     constants/wallet-connect-eth-sign-typed-method}
   method))
