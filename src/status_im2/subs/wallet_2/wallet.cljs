(ns status-im2.subs.wallet-2.wallet
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]))

(defn calculate-raw-balance
  [raw-balance decimals]
  (let [raw-number (js/parseInt raw-balance)]
    (if (not (js/isNaN raw-number))
      (/ raw-number (Math/pow 10 (js/parseInt decimals)))
      0)))

(defn calculate-balance
  [address tokens]
  (let [token  (get tokens (keyword (string/lower-case address)))
        result (reduce
                (fn [acc item]
                  (let [total-value-per-token (reduce (fn [ac balances]
                                                        (+ (calculate-raw-balance (:rawBalance balances)
                                                                                  (:decimals item))
                                                           ac))
                                                      0
                                                      (vals (:balancesPerChain item)))
                        total-values          (* total-value-per-token
                                                 (get-in item [:marketValuesPerCurrency :USD :price]))]
                    (+ acc total-values)))
                0
                token)]
    (.toFixed result 2)))

(re-frame/reg-sub
 :wallet-2/balances
 :<- [:profile/wallet-accounts]
 :<- [:wallet-2/tokens]
 (fn [[accounts tokens]]
   (for [account accounts]
     (let [address (:address account)]
       {:address address
        :balance (calculate-balance address tokens)}))))
