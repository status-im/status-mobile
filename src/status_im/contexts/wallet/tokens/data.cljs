(ns status-im.contexts.wallet.tokens.data
  (:require [camel-snake-kebab.extras :as cske]
            [clojure.set :as set]
            [utils.transforms :as transforms]))

(defn- normalize-by-symbol
  [token-source]
  (->> token-source
       :tokens
       (reduce (fn [acc token-data]
                 (let [chain-id     (:chainId token-data)
                       token-symbol (-> token-data :symbol keyword)]
                   (->> token-data
                        (cske/transform-keys transforms/->kebab-case-keyword)
                        (update acc chain-id assoc token-symbol))))
               {})))

(defn- flat-symbol-list
  [token-source]
  (->> token-source
       :tokens
       (reduce (fn [acc token-data]
                 (let [chain-id     (:chainId token-data)
                       token-symbol (-> token-data :symbol keyword)]
                   (update acc chain-id set/union #{token-symbol})))
               {})))

(defn- sort-symbol-list
  [symbol-map]
  (reduce (fn [acc [chain-id symbol-list]]
            (assoc acc chain-id (-> symbol-list sort vec)))
          {}
          symbol-map))

(defn normalize-tokens
  [{:keys [data]}]
  (-> (reduce
       (fn [{:keys [sources tokens-by-symbol symbol-lists]} token-source]
         (let [{:keys [source version name]} token-source]
           {:sources          (conj sources
                                    {:name    name
                                     :source  source
                                     :version version})
            :tokens-by-symbol (merge-with merge tokens-by-symbol (normalize-by-symbol token-source))
            :symbol-lists     (merge-with set/union symbol-lists (flat-symbol-list token-source))}))
       {}
       data)
      (update :symbol-lists sort-symbol-list)))

(defn all-token-symbols
  [symbol-lists]
  (->> symbol-lists
       vals
       (map set)
       (apply set/union)
       vec))

(defn normalize-market-values
  [market-values]
  (reduce-kv (fn [acc token-symbol data]
               (assoc acc
                      token-symbol
                      {:market-cap      (:MKTCAP data)
                       :high-day        (:HIGHDAY data)
                       :low-day         (:LOWDAY data)
                       :change-pct-hour (:CHANGEPCTHOUR data)
                       :change-pct-day  (:CHANGEPCTDAY data)
                       :change-pct-24h  (:CHANGEPCT24HOUR data)
                       :change-24h      (:CHANGE24HOUR data)}))
             {}
             market-values))

(defn normalize-token-details
  [token-details]
  (reduce-kv (fn [acc token-symbol data]
               (->> data
                    (cske/transform-keys transforms/->kebab-case-keyword)
                    (assoc acc token-symbol)))
             {}
             token-details))

(defn normalize-prices
  [token-prices]
  (reduce-kv
   (fn [acc k v]
     (->> v
          (map (fn [[currency price]]
                 [currency {:price price}]))
          (into {})
          (assoc acc k)))
   {}
   token-prices))
