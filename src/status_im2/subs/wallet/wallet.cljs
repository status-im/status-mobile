(ns status-im2.subs.wallet.wallet
  (:require [clojure.string :as string]
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
=======
>>>>>>> 14fc5af12 (qa)
 :wallet/accounts
 :<- [:wallet]
 :-> #(->> %
           :accounts
           vals
           (sort-by :position)))

(rf/reg-sub
<<<<<<< HEAD
=======
>>>>>>> 97b751fe9 (rebase)
=======
>>>>>>> a209a1368 (lint)
=======
>>>>>>> 2cd7438de (review)
=======
>>>>>>> 411271469 (lint)
=======
>>>>>>> 14fc5af12 (qa)
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
 (fn [[{:keys [current-viewing-account-address] :as wallet} balances]]
   (-> wallet
       (get-in [:accounts current-viewing-account-address])
       (assoc :balance (get balances current-viewing-account-address)))))

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
=======
>>>>>>> f4f0deecf (review)
  :wallet/accounts
  :<- [:wallet]
  :-> #(->> %
            :accounts
            vals
            (sort-by :position)))
=======
 :wallet/accounts
 :<- [:wallet]
 :-> #(->> %
           :accounts
           vals
           (sort-by :position)))
>>>>>>> aed578b59 (lint)
=======
>>>>>>> 2d40168d5 (qa)


(defn- calc-token-value
  [{:keys [symbol market-values-per-currency] :as item} chain-id]

  (let [crypto-value                      (utils/token-value-in-chain item chain-id)
        market-values                     (:usd market-values-per-currency)
        {:keys [price change-pct-24hour]} market-values
        fiat-change                       (utils/calculate-fiat-change crypto-value change-pct-24hour)]
    (when crypto-value
      {:token               (keyword (string/lower-case symbol))
       :state               :default
       :status              (cond
                              (pos? change-pct-24hour) :positive
                              (neg? change-pct-24hour) :negative
                              :else                    :empty)
       :customization-color :blue
       :values              {:crypto-value      crypto-value
                             :fiat-value        (utils/prettify-balance (* crypto-value price))
                             :percentage-change (.toFixed change-pct-24hour 2)
                             :fiat-change       (utils/prettify-balance fiat-change)}})))

<<<<<<< HEAD
<<<<<<< HEAD
(re-frame/reg-sub
 :wallet/account-token-values
<<<<<<< HEAD
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
<<<<<<< HEAD
 :<- [:wallet/tokens]
 (fn [[{:keys [current-viewing-account-address]} tokens]]
   (mapv calc-token-value (get tokens (keyword (string/lower-case current-viewing-account-address))))))
>>>>>>> a61095482 (lint)
<<<<<<< HEAD
>>>>>>> c5e853c4e (lint)
=======
=======
 :<- [:wallet/accounts]
 (fn [[{:keys [current-viewing-account-address]} accounts]]
   (let [current-account (first (filter #(= current-viewing-account-address (:address %)) accounts))]
   (mapv calc-token-value (:tokens current-account)))))
>>>>>>> 30509f66a (fix)
<<<<<<< HEAD
>>>>>>> 2a4f6150e (fix)
=======
=======
=======
(rf/reg-sub
 :wallet/account-token-values
>>>>>>> 2d40168d5 (qa)
 :<- [:wallet/current-viewing-account]
 :<- [:chain-id]
 (fn [[current-account chain-id]]
   (mapv #(calc-token-value % chain-id) (:tokens current-account))))
<<<<<<< HEAD
>>>>>>> f4f0deecf (review)
>>>>>>> a544469d8 (review)
=======
>>>>>>> 2d40168d5 (qa)
