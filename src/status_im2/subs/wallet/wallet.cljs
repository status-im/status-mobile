(ns status-im2.subs.wallet.wallet
<<<<<<< HEAD
<<<<<<< HEAD
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
=======
<<<<<<< HEAD
  (:require [re-frame.core :as rf]
>>>>>>> af0e5cc43 (review)
            [status-im2.contexts.wallet.common.utils :as utils]
            [utils.number]))
=======
=======
>>>>>>> 6acd5d275 (rebase)
  (:require
    [clojure.string :as string]
    [re-frame.core :as rf]
    [status-im2.contexts.wallet.common.utils :as utils]
    [utils.number]))

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
<<<<<<< HEAD
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
                                        (get-in item [:market-values-per-currency :USD :price]))]
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
=======
>>>>>>> 97b751fe9 (rebase)
=======
>>>>>>> a209a1368 (lint)
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
<<<<<<< HEAD

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
>>>>>>> 6acd5d275 (rebase)
=======
  :wallet/accounts
  :<- [:wallet]
  :-> #(->> %
            :accounts
            vals
            (sort-by :position)))
>>>>>>> 206a158a1 (rebase)
=======
 :wallet/accounts
 :<- [:wallet]
 :-> #(->> %
           :accounts
           vals
           (sort-by :position)))
>>>>>>> 68d91b323 (lint)

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
   (mapv (fn [{:keys [color address] :as account}]
           (assoc account
                  :customization-color color
                  :type                :empty
                  :on-press            #(rf/dispatch [:wallet/navigate-to-account address])
                  :loading?            tokens-loading?
                  :balance             (utils/prettify-balance (get balances address))))
         accounts)))

(rf/reg-sub
 :wallet/current-viewing-account
 :<- [:wallet]
 :<- [:wallet/balances]
 (fn [[{:keys [current-viewing-account-address] :as wallet} balances]]
   (-> wallet
       (get-in [:accounts current-viewing-account-address])
       (assoc :balance (get balances current-viewing-account-address)))))

(defn- calc-token-value
  [{:keys [symbol market-values-per-currency] :as item}]
  (let [fiat-value                        (utils/total-token-value-in-all-chains item)
        market-values                     (:usd market-values-per-currency)
        {:keys [price change-pct-24hour]} market-values
        fiat-change                       (utils/calculate-fiat-change fiat-value change-pct-24hour)]
    {:token               (keyword (string/lower-case symbol))
     :state               :default
     :status              (cond
                            (pos? change-pct-24hour) :positive
                            (neg? change-pct-24hour) :negative
                            :else                    :empty)
     :customization-color :blue
     :values              {:crypto-value      (.toFixed (* fiat-value price) 2)
                           :fiat-value        (utils/prettify-balance fiat-value)
                           :percentage-change (.toFixed change-pct-24hour 2)
                           :fiat-change       (utils/prettify-balance fiat-change)}}))

<<<<<<< HEAD
(re-frame/reg-sub
 :wallet/account-token-values
 :<- [:wallet]
 :<- [:wallet/tokens]
<<<<<<< HEAD
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
<<<<<<< HEAD
>>>>>>> fd4728c35 (review)
<<<<<<< HEAD
>>>>>>> 3c3a704c6 (review)
=======
=======
=======
 (fn [[{:keys [current-viewing-account-address]} tokens]]
   (mapv calc-token-value (get tokens (keyword (string/lower-case current-viewing-account-address))))))
>>>>>>> aeda1e4a7 (review)
>>>>>>> af0e5cc43 (review)
<<<<<<< HEAD
>>>>>>> fa6b6eb69 (review)
=======
=======
(rf/reg-sub
<<<<<<< HEAD
  :wallet/account-token-values
  :<- [:wallet]
  :<- [:wallet/tokens]
  (fn [[{:keys [current-viewing-account-address]} tokens]]
    (mapv calc-token-value (get tokens (keyword (string/lower-case current-viewing-account-address))))))
>>>>>>> 6acd5d275 (rebase)
<<<<<<< HEAD
>>>>>>> d33622510 (rebase)
=======
=======
 :wallet/account-token-values
 :<- [:wallet]
 :<- [:wallet/tokens]
 (fn [[{:keys [current-viewing-account-address]} tokens]]
   (mapv calc-token-value (get tokens (keyword (string/lower-case current-viewing-account-address))))))
>>>>>>> a61095482 (lint)
>>>>>>> c5e853c4e (lint)
