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
 :wallet/watch-address-activity-state
 :<- [:wallet/ui]
 :-> :watch-address-activity-state)

(rf/reg-sub
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
 (fn [[accounts balances _]]
   (mapv (fn [{:keys [color address] :as account}]
           (assoc account
                  :customization-color color
                  :type                (if (= type :watch) :watch-only :empty)
                  :on-press            #(rf/dispatch [:wallet/navigate-to-account address])
                  :loading?            false
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
                              :total-balance-fiat (utils/calculate-balance-for-token token)))
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
