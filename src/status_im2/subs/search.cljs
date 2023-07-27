(ns status-im2.subs.search
  (:require [clojure.string :as string])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.utils.currency :as currency]))

(defn sort-by-timestamp
  [coll]
  (when (not-empty coll)
    (sort-by #(-> % second :timestamp)
             >
             (into {} coll))))

(defn apply-filter
  "extract-attributes-fn is a function that take an element from the collection
  and returns a vector of attributes which are strings
  apply-filter returns the elements for which at least one attribute includes
  the search-filter
  apply-filter returns nil if there is no element that match the filter
  apply-filter returns full collection if the search-filter is empty"
  [search-filter coll extract-attributes-fn sort?]
  (let [results (if (not-empty search-filter)
                  (let [search-filter (string/lower-case search-filter)]
                    (filter (fn [element]
                              (some (fn [v]
                                      (let [s (cond (string? v)  v
                                                    (keyword? v) (name v))]
                                        (when (string? s)
                                          (string/includes? (string/lower-case s)
                                                            search-filter))))
                                    (extract-attributes-fn element)))
                            coll))
                  coll)]
    (if sort?
      (sort-by-timestamp results)
      results)))

(defn extract-currency-attributes
  [currency]
  (let [{:keys [code display-name]} (val currency)]
    [code display-name]))

(re-frame/reg-sub
 :search/filtered-currencies
 :<- [:search/currency-filter]
 (fn [search-currency-filter]
   {:search-filter search-currency-filter
    :currencies    (apply-filter search-currency-filter
                                 currency/currencies
                                 extract-currency-attributes
                                 false)}))

(defn extract-token-attributes
  [token]
  [(:symbol token) (:name token)])

(re-frame/reg-sub
 :wallet/filtered-grouped-chain-tokens
 :<- [:wallet/grouped-chain-tokens]
 :<- [:search/token-filter]
 (fn [[{custom-tokens true default-tokens nil} search-token-filter]]
   {:search-filter search-token-filter
    :tokens        {true (apply-filter search-token-filter custom-tokens extract-token-attributes false)
                    nil  (apply-filter search-token-filter
                                       default-tokens
                                       extract-token-attributes
                                       false)}}))
