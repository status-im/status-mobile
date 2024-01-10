(ns status-im.subs.wallet.wallet
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.contexts.wallet.common.utils :as utils]
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
 :wallet/wallet-send-to-address
 :<- [:wallet/wallet-send]
 :-> :to-address)

(rf/reg-sub
 :wallet/wallet-send-route
 :<- [:wallet/wallet-send]
 :-> :route)

(rf/reg-sub
 :wallet/wallet-send-token
 :<- [:wallet/wallet-send]
 :-> :token)

(rf/reg-sub
 :wallet/wallet-send-loading-suggested-routes?
 :<- [:wallet/wallet-send]
 :-> :loading-suggested-routes?)

(rf/reg-sub
 :wallet/wallet-send-suggested-routes
 :<- [:wallet/wallet-send]
 :-> :suggested-routes)

(rf/reg-sub
 :wallet/watch-address-activity-state
 :<- [:wallet/ui]
 :-> :watch-address-activity-state)

(rf/reg-sub
 :wallet/accounts
 :<- [:wallet]
 :<- [:wallet/network-details]
 :<- [:profile/test-networks-enabled?]
 (fn [[wallet network-details test-networks-enabled?]]
   (->> wallet
        :accounts
        vals
        (map #(assoc-network-preferences-names network-details % test-networks-enabled?))
        (sort-by :position))))

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
 :<- [:profile/currency]
 (fn [[accounts currency]]
   (zipmap (map :address accounts)
           (map #(utils/calculate-balance-for-account currency %) accounts))))

(rf/reg-sub
 :wallet/account-cards-data
 :<- [:wallet/accounts]
 :<- [:wallet/balances]
 :<- [:wallet/tokens-loading?]
 :<- [:profile/currency-symbol]
 (fn [[accounts balances tokens-loading? currency-symbol]]
   (mapv (fn [{:keys [color address watch-only?] :as account}]
           (assoc account
                  :customization-color color
                  :type                (if watch-only? :watch-only :empty)
                  :on-press            #(rf/dispatch [:wallet/navigate-to-account address])
                  :loading?            tokens-loading?
                  :balance             (utils/prettify-balance currency-symbol (get balances address))))
         accounts)))

(rf/reg-sub
 :wallet/current-viewing-account
 :<- [:wallet/accounts]
 :<- [:wallet/current-viewing-account-address]
 :<- [:wallet/balances]
 :<- [:profile/currency-symbol]
 (fn [[accounts current-viewing-account-address balances currency-symbol]]
   (let [current-viewing-account (utils/get-account-by-address accounts current-viewing-account-address)
         balance                 (get balances current-viewing-account-address)
         formatted-balance       (utils/prettify-balance currency-symbol balance)]
     (-> current-viewing-account
         (assoc :balance           balance
                :formatted-balance formatted-balance)))))

(rf/reg-sub
 :wallet/tokens-filtered
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/network-details]
 (fn [[account networks] [_ query]]
   (let [tokens          (map (fn [token]
                                (assoc token
                                       :networks           (utils/network-list token networks)
                                       :total-balance      (utils/total-token-units-in-all-chains token)
                                       :total-balance-fiat (utils/calculate-balance-for-token token)))
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

(rf/reg-sub
 :wallet/accounts-without-watched-accounts
 :<- [:wallet/accounts]
 (fn [accounts]
   (remove #(:watch-only? %) accounts)))

(rf/reg-sub
 :wallet/account-token-values
 :<- [:wallet/current-viewing-account]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[{:keys [tokens color]} currency currency-symbol]]
   (mapv #(utils/calculate-token-value {:token           %
                                        :color           color
                                        :currency        currency
                                        :currency-symbol currency-symbol})
         tokens)))

(rf/reg-sub
 :wallet/aggregated-tokens
 :<- [:wallet/accounts]
 (fn [accounts]
   (utils/aggregate-tokens-for-all-accounts accounts)))

(rf/reg-sub
 :wallet/aggregated-tokens-and-balance
 :<- [:wallet/aggregated-tokens]
 :<- [:profile/customization-color]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[aggregated-tokens color currency currency-symbol]]
   (let [balance           (utils/calculate-balance-from-tokens {:currency currency
                                                                 :tokens   aggregated-tokens})
         formatted-balance (utils/prettify-balance currency-symbol balance)]
     {:balance           balance
      :formatted-balance formatted-balance
      :tokens            (mapv #(utils/calculate-token-value {:token           %
                                                              :color           color
                                                              :currency        currency
                                                              :currency-symbol currency-symbol})
                               aggregated-tokens)})))

(rf/reg-sub
 :wallet/network-preference-details
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/network-details]
 (fn [[current-viewing-account network-details]]
   (let [network-preferences-names (:network-preferences-names current-viewing-account)]
     (filter #(contains? network-preferences-names (:network-name %)) network-details))))

(rf/reg-sub
 :wallet/accounts-with-customization-color
 :<- [:wallet/accounts]
 (fn [accounts]
   (map (fn [{:keys [color] :as account}]
          (assoc account :customization-color color))
        accounts)))
