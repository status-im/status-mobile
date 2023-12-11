(ns status-im2.subs.wallet.wallet
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im2.contexts.wallet.common.utils :as utils]
            [utils.number]))

(defn- filter-networks
  [chain-ids network-details]
  (filter (fn [{:keys [chain-id]}]
            (contains? chain-ids chain-id))
          network-details))

(defn- assoc-network-preferences-names
  [network-details account testnet?]
  (let [{:keys [prod-preferred-chain-ids
                test-preferred-chain-ids]} account
        current-chain-ids                  (if testnet?
                                             test-preferred-chain-ids
                                             prod-preferred-chain-ids)
        network-preferences-names          (->> network-details
                                                (filter-networks current-chain-ids)
                                                (map :network-name)
                                                (set))]
    (assoc account :network-preferences-names network-preferences-names)))

(rf/reg-sub
 :wallet/ui
 :<- [:wallet]
 :-> :ui)

(rf/reg-sub
 :wallet/tokens-loading?
 :<- [:wallet/ui]
 :-> :tokens-loading?)


(rf/reg-sub
 :wallet/current-viewing-account-address
 :<- [:wallet]
 :-> :current-viewing-account-address)

(rf/reg-sub
 :wallet/watch-address-activity-state
 :<- [:wallet/ui]
 :-> :watch-address-activity-state)

(rf/reg-sub
 :wallet/accounts
 :<- [:wallet]
 :<- [:wallet/network-details]
 (fn [[wallet network-details]]
   ;; TODO(@rende11): `testnet?` value would be relevant after this implementation,
   ;; https://github.com/status-im/status-mobile/issues/17826
   (let [testnet? false]
     (->> wallet
          :accounts
          vals
          (map #(assoc-network-preferences-names network-details % testnet?))
          (sort-by :position)))))

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
           (map utils/calculate-balance-for-account accounts))))

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
 :<- [:wallet/accounts]
 :<- [:wallet/current-viewing-account-address]
 :<- [:wallet/balances]
 (fn [[accounts current-viewing-account-address balances]]
   (let [current-viewing-account (utils/get-account-by-address accounts current-viewing-account-address)]
     (-> current-viewing-account
         (assoc :balance (get balances current-viewing-account-address))))))

(rf/reg-sub
 :wallet/tokens-filtered
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/network-details]
 (fn [[account networks] [_ query]]
   (let [tokens          (map (fn [token]
                                (assoc token
                                       :networks           (utils/network-list token networks)
                                       :total-balance      (utils/total-token-units-in-all-chains token)
                                       :total-balance-fiat 0))
                              (:tokens account))
         sorted-tokens   (sort-by :name compare tokens)
         filtered-tokens (filter #(or (string/starts-with? (string/lower-case (:name %))
                                                           (string/lower-case query))
                                      (string/starts-with? (string/lower-case (:symbol %))
                                                           (string/lower-case query)))
                                 sorted-tokens)]
     filtered-tokens)))

(rf/reg-sub
 :wallet/accounts-without-current-viewing-account
 :<- [:wallet/accounts]
 :<- [:wallet/current-viewing-account-address]
 (fn [[accounts current-viewing-account-address]]
   (remove #(= (:address %) current-viewing-account-address) accounts)))

(defn- calc-token-value
  [{:keys [market-values-per-currency] :as item} chain-id]
  (let [crypto-value                      (utils/token-value-in-chain item chain-id)
        market-values                     (:usd market-values-per-currency)
        {:keys [price change-pct-24hour]} market-values
        fiat-change                       (utils/calculate-fiat-change crypto-value change-pct-24hour)]
    (when (and crypto-value (not (empty? (:name item))))
      {:token               (keyword (string/lower-case (:symbol item)))
       :token-name          (:name item)
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

(rf/reg-sub
 :wallet/account-token-values
 :<- [:wallet/current-viewing-account]
 :<- [:chain-id]
 (fn [[current-account chain-id]]
   (mapv #(calc-token-value % chain-id) (:tokens current-account))))

(rf/reg-sub
 :wallet/network-preference-details
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/network-details]
 (fn [[current-viewing-account network-details]]
   (let [network-preferences-names (:network-preferences-names current-viewing-account)]
     (filter #(contains? network-preferences-names (:network-name %)) network-details))))
