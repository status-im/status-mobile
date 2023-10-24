(ns status-im2.subs.wallet-2.wallet
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im2.contexts.wallet.common.utils :as utils]
            [utils.number]))

(defn- calculate-raw-balance
  [raw-balance decimals]
  (if-let [n (utils.number/parse-int raw-balance nil)]
    (/ n (Math/pow 10 (utils.number/parse-int decimals)))
    0))

(defn- total-per-token
  [item]
  (reduce (fn [ac balances]
            (+ (calculate-raw-balance (:rawBalance balances)
                                      (:decimals item))
               ac))
          0
          (vals (:balancesPerChain item))))

(defn- calculate-balance
  [address tokens]
  (let [token  (get tokens (keyword (string/lower-case address)))
        result (reduce
                (fn [acc item]
                  (let [total-values (* (total-per-token item)
                                        (get-in item [:marketValuesPerCurrency :USD :price]))]
                    (+ acc total-values)))
                0
                token)]
    result))

(re-frame/reg-sub
 :wallet-2/balances
 :<- [:profile/wallet-accounts]
 :<- [:wallet-2/tokens]
 (fn [[accounts tokens]]
   (for [{:keys [address]} accounts]
     {:address address
      :balance (calculate-balance address tokens)})))

(re-frame/reg-sub
 :wallet-2/account
 :<- [:profile/wallet-accounts]
 :<- [:wallet-2/balances]
 (fn [[accounts balances] [_ account-address]]
   (assoc (utils/get-account-by-address accounts account-address) 
          :balance (utils/get-balance-by-address balances account-address))))
