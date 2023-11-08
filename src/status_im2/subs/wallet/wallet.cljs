(ns status-im2.subs.wallet.wallet
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im2.contexts.wallet.common.utils :as utils]
            [utils.number]))

<<<<<<< HEAD
(rf/reg-sub
 :wallet/ui
 :<- [:wallet]
 :-> :ui)

(rf/reg-sub
 :wallet/tokens-loading?
 :<- [:wallet/ui]
 :-> :tokens-loading?)

(rf/reg-sub
<<<<<<< HEAD
 :wallet/watch-address-activity-state
 :<- [:wallet/ui]
 :-> :watch-address-activity-state)

(rf/reg-sub
=======
=======
(defn- calculate-balance
  [address tokens]
  (let [token  (get tokens (keyword address))
        result (reduce
                (fn [acc item]
                  (let [total-values (* (utils/sum-token-chains item)
                                        (get-in item [:marketValuesPerCurrency :USD :price]))]
                    (+ acc total-values)))
                0
                token)]
    result))

(re-frame/reg-sub
>>>>>>> c8bb0a581 (updates)
>>>>>>> 302c755ce (updates)
 :wallet/accounts
 :<- [:wallet]
 :-> #(->> %
           :accounts
           vals
           (sort-by :position)))

(rf/reg-sub
 :wallet/addresses
 :<- [:wallet]
 :-> #(->> %
           :accounts
           keys
           set))

(rf/reg-sub
 :wallet/balances
 :<- [:wallet/accounts]
 (fn [accounts]
   (zipmap (map :address accounts)
           (map #(-> % :tokens utils/calculate-balance) accounts))))

(rf/reg-sub
 :wallet/account-cards-data
 :<- [:wallet/accounts]
 :<- [:wallet/balances]
 :<- [:wallet/tokens-loading?]
 (fn [[accounts balances tokens-loading?]]
   (mapv (fn [{:keys [color address type] :as account}]
           (assoc account
                  :customization-color color
                  :type                (if (= type :watch) :watch-only :empty)
                  :on-press            #(rf/dispatch [:wallet/navigate-to-account address])
                  :loading?            tokens-loading?
                  :balance             (utils/prettify-balance (get balances address))))
         accounts)))

(rf/reg-sub
 :wallet/current-viewing-account
 :<- [:wallet]
 :<- [:wallet/balances]
<<<<<<< HEAD
 (fn [[{:keys [current-viewing-account-address] :as wallet} balances]]
   (-> wallet
       (get-in [:accounts current-viewing-account-address])
<<<<<<< HEAD
       (assoc :balance (get balances current-viewing-account-address)))))
<<<<<<< HEAD

(rf/reg-sub
 :wallet/tokens-filtered
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/network-details]
 (fn [[account networks] [_ query]]
   (let [tokens (map (fn [token]
                       (assoc token
                              :networks           (utils/network-list token networks)
                              :total-balance      (utils/total-token-value-in-all-chains token)
                              :total-balance-fiat (utils/calculate-balance token)))
                     (:tokens account))

         sorted-tokens
         (sort-by :name compare tokens)
         filtered-tokens
         (filter #(or (string/starts-with? (string/lower-case (:name %))
                                           (string/lower-case query))
                      (string/starts-with? (string/lower-case (:symbol %))
                                           (string/lower-case query)))
                 sorted-tokens)]
     filtered-tokens)))
<<<<<<< HEAD

(rf/reg-sub
 :wallet/current-viewing-account-address
 :<- [:wallet]
 :-> :current-viewing-account-address)

(rf/reg-sub
 :wallet/accounts-without-current-viewing-account
 :<- [:wallet/accounts]
 :<- [:wallet/current-viewing-account-address]
 (fn [[accounts current-viewing-account-address]]
   (remove #(= (:address %) current-viewing-account-address) accounts)))
=======
=======
=======
       (assoc :balance (utils/get-balance-by-address balances current-viewing-account-address)))))
=======
 (fn [[accounts balances] [_ account-address]]
   (assoc
    (utils/get-account-by-address accounts account-address)
    :balance
    (utils/get-balance-by-address balances account-address))))

(defn- calc-token-value
  [{:keys [symbol market-values-per-currency] :as item}]
  (let [fiat-value                      (utils/sum-token-chains item)
        market-values                   (:usd market-values-per-currency)
        {:keys [price change-pct-24-hour]} market-values
        fiat-change                     (* fiat-value (/ change-pct-24-hour (+ 100 change-pct-24-hour)))]
    {:token               (keyword (string/lower-case symbol))
     :state               :default
     :status              (cond
                            (pos? change-pct-24-hour) :positive
                            (neg? change-pct-24-hour) :negative
                            :else                  :empty)
     :customization-color :blue
     :values              {:crypto-value      (.toFixed (* fiat-value price) 2)
                           :fiat-value        (utils/prettify-balance fiat-value)
                           :percentage-change (.toFixed change-pct-24-hour 2)
                           :fiat-change       (utils/prettify-balance fiat-change)}}))

(re-frame/reg-sub
 :wallet/token-values
 :<- [:wallet/tokens]
 (fn [tokens [_ account-address]]
<<<<<<< HEAD
   (mapv prepare-token (get tokens (keyword (string/lower-case account-address))))))
>>>>>>> 19c75e91d (review)
<<<<<<< HEAD
>>>>>>> 25ec47428 (review)
<<<<<<< HEAD
>>>>>>> e41fe5426 (review)
<<<<<<< HEAD
>>>>>>> aaf682999 (review)
=======
=======
=======
=======
   (mapv calc-token-value (get tokens (keyword (string/lower-case account-address))))))
>>>>>>> 5ec5c0e69 (review)
>>>>>>> af0d365b4 (review)
>>>>>>> fd4728c35 (review)
>>>>>>> 3c3a704c6 (review)
