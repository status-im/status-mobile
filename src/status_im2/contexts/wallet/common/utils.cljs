(ns status-im2.contexts.wallet.common.utils
  (:require
    [clojure.string :as string]))

(defn get-first-name
  [full-name]
  (first (string/split full-name #" ")))

(defn calculate-raw-balance
  [raw-balance decimals]
  (if (not (js/isNaN (js/parseInt raw-balance)))
    (/ (js/parseInt raw-balance) (Math/pow 10 (js/parseInt decimals))) 0))

(defn calculate-balance
  [address tokens]
  (let [token        (get tokens (keyword (string/lower-case address)))
        result       (reduce (fn [acc item]
                               (let [total-value-per-token (reduce (fn [ac balances]
                                                                     (+ (calculate-raw-balance (:rawBalance balances)
                                                                                               (:decimals item))
                                                                        ac))
                                                                   0
                                                                   (vals (:balancesPerChain item)))
                                     total-values (* total-value-per-token
                                                     (get-in item [:marketValuesPerCurrency :USD :price]))]
                                 (+ acc total-values)))
                             0
                             token)]
    (.toFixed result 2)))
