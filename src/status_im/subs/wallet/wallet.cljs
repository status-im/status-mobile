(ns status-im.subs.wallet.wallet
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.constants :as constants]
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
 :wallet/wallet-send
 :<- [:wallet/ui]
 :-> :send)

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
 :wallet/wallet-send-amount
 :<- [:wallet/wallet-send]
 :-> :amount)

(rf/reg-sub
 :wallet/wallet-send-loading-suggested-routes?
 :<- [:wallet/wallet-send]
 :-> :loading-suggested-routes?)

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

(defn count-trailing-zeroes
  [num]
  (let [str-representation (str num)
        decimal-part       (second (clojure.string/split str-representation #"\."))
        count              (count (take-while #(= \0 %) decimal-part))]
    (if-let [first-non-zero-digit (first (filter #(not (= \0 %)) decimal-part))]
      (if (= \1 first-non-zero-digit)
        (inc count)
        count)
      count)))

(defn- calc-token-value
  [{:keys [market-values-per-currency] :as token} color currency currency-symbol]
  (let [token-units                 (utils/total-token-units-in-all-chains token)
        fiat-value                  (utils/total-token-fiat-value currency token)
        market-values               (get market-values-per-currency
                                         currency
                                         (get market-values-per-currency
                                              constants/profile-default-currency))
        {:keys [change-pct-24hour]} market-values]
    {:token               (:symbol token)
     :token-name          (:name token)
     :state               :default
     :status              (cond
                            (pos? change-pct-24hour) :positive
                            (neg? change-pct-24hour) :negative
                            :else                    :empty)
     :customization-color color
     :values              {:crypto-value (utils/prettify-crypto market-values-per-currency token-units)
                           :fiat-value   (utils/prettify-balance currency-symbol fiat-value)}}))

(rf/reg-sub
 :wallet/account-token-values
 :<- [:wallet/current-viewing-account]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[{:keys [tokens color]} currency currency-symbol]]
   (mapv #(calc-token-value % color currency currency-symbol) tokens)))

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
