(ns status-im.subs.wallet.wallet
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.subs.wallet.add-account.address-to-watch]
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
 :wallet/scanned-address
 :<- [:wallet/ui]
 :-> :scanned-address)

(rf/reg-sub
 :wallet/tokens-loading?
 :<- [:wallet/ui]
 :-> :tokens-loading?)

(rf/reg-sub
 :wallet/create-account
 :<- [:wallet/ui]
 :-> :create-account)

(rf/reg-sub
 :wallet/network-filter
 :<- [:wallet/ui]
 :-> :network-filter)

(rf/reg-sub
 :wallet/selected-networks
 :<- [:wallet/network-filter]
 :-> :selected-networks)

(rf/reg-sub
 :wallet/network-filter-selector-state
 :<- [:wallet/network-filter]
 :-> :selector-state)

(rf/reg-sub
 :wallet/current-viewing-account-address
 :<- [:wallet]
 :-> :current-viewing-account-address)

(rf/reg-sub
 :wallet/viewing-account?
 :<- [:wallet/current-viewing-account-address]
 (fn [address]
   (boolean address)))

(rf/reg-sub
 :wallet/wallet-send-to-address
 :<- [:wallet/wallet-send]
 :-> :to-address)

(rf/reg-sub
 :wallet/wallet-send-address-prefix
 :<- [:wallet/wallet-send]
 :-> :address-prefix)

(rf/reg-sub
 :wallet/wallet-send-selected-networks
 :<- [:wallet/wallet-send]
 :-> :selected-networks)

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
 :wallet/wallet-send-suggested-routes
 :<- [:wallet/wallet-send]
 :-> :suggested-routes)

(rf/reg-sub
 :wallet/wallet-bridge-to-chain-id
 :<- [:wallet/wallet-send]
 :-> :bridge-to-chain-id)

(rf/reg-sub
 :wallet/keypairs
 :<- [:wallet]
 :-> :keypairs)

(rf/reg-sub
 :wallet/selected-keypair-uid
 :<- [:wallet/create-account]
 :-> :selected-keypair-uid)

(rf/reg-sub
 :wallet/selected-networks->chain-ids
 :<- [:wallet/selected-networks]
 :<- [:profile/test-networks-enabled?]
 :<- [:profile/is-goerli-enabled?]
 (fn [[selected-networks testnet-enabled? goerli-enabled?]]
   (set (map #(utils/network->chain-id
               {:network          %
                :testnet-enabled? testnet-enabled?
                :goerli-enabled?  goerli-enabled?})
             selected-networks))))

(rf/reg-sub
 :wallet/derivation-path-state
 :<- [:wallet/create-account]
 :-> :derivation-path-state)

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
 :wallet/watch-only-accounts
 :<- [:wallet/accounts]
 (fn [accounts]
   (filter :watch-only? accounts)))

(rf/reg-sub
 :wallet/addresses
 :<- [:wallet]
 :-> #(->> %
           :accounts
           keys
           set))

(rf/reg-sub
 :wallet/balances-in-selected-networks
 :<- [:wallet/accounts]
 :<- [:profile/currency]
 :<- [:wallet/selected-networks->chain-ids]
 (fn [[accounts currency chain-ids]]
   (zipmap (map :address accounts)
           (map #(utils/calculate-balance-from-tokens {:currency  currency
                                                       :tokens    (:tokens %)
                                                       :chain-ids chain-ids})
                accounts))))

(rf/reg-sub
 :wallet/account-cards-data
 :<- [:wallet/accounts]
 :<- [:wallet/balances-in-selected-networks]
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
 :<- [:wallet/balances-in-selected-networks]
 :<- [:profile/currency-symbol]
 (fn [[accounts current-viewing-account-address balances currency-symbol]]
   (let [balance           (get balances current-viewing-account-address)
         formatted-balance (utils/prettify-balance currency-symbol balance)]
     (-> accounts
         (utils/get-account-by-address current-viewing-account-address)
         (assoc :balance           balance
                :formatted-balance formatted-balance)))))

(rf/reg-sub
 :wallet/current-viewing-account-tokens-in-selected-networks
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/selected-networks->chain-ids]
 (fn [[{:keys [tokens]} chain-ids]]
   (utils/filter-tokens-in-chains tokens chain-ids)))

(rf/reg-sub
 :wallet/current-viewing-account-tokens-filtered
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/network-details]
 (fn [[account networks] [_ query]]
   (let [tokens        (map (fn [token]
                              (assoc token
                                     :networks      (utils/network-list token networks)
                                     :total-balance (utils/calculate-total-token-balance token)))
                            (:tokens account))
         sorted-tokens (sort-by :name compare tokens)]
     (if query
       (let [query-string (string/lower-case query)]
         (filter #(or (string/starts-with? (string/lower-case (:name %)) query-string)
                      (string/starts-with? (string/lower-case (:symbol %)) query-string))
                 sorted-tokens))
       sorted-tokens))))

(rf/reg-sub
 :wallet/token-by-symbol
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/network-details]
 (fn [[account networks] [_ token-symbol]]
   (let [tokens (map (fn [token]
                       (assoc token
                              :networks      (utils/network-list token networks)
                              :total-balance (utils/calculate-total-token-balance token)))
                     (:tokens account))
         token  (first (filter #(= (string/lower-case (:symbol %))
                                   (string/lower-case token-symbol))
                               tokens))]
     token)))

(rf/reg-sub
 :wallet/accounts-without-current-viewing-account
 :<- [:wallet/accounts]
 :<- [:wallet/current-viewing-account-address]
 (fn [[accounts current-viewing-account-address]]
   (remove #(= (:address %) current-viewing-account-address) accounts)))

(rf/reg-sub
 :wallet/accounts-without-watched-accounts
 :<- [:wallet/accounts-with-customization-color]
 (fn [accounts]
   (remove :watch-only? accounts)))

(rf/reg-sub
 :wallet/current-viewing-account-token-values
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/current-viewing-account-tokens-in-selected-networks]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[{:keys [color]} tokens currency currency-symbol]]
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
 :wallet/aggregated-tokens-in-selected-networks
 :<- [:wallet/aggregated-tokens]
 :<- [:wallet/selected-networks->chain-ids]
 (fn [[aggregated-tokens chain-ids]]
   (utils/filter-tokens-in-chains aggregated-tokens chain-ids)))

(rf/reg-sub
 :wallet/aggregated-token-values-and-balance
 :<- [:wallet/aggregated-tokens-in-selected-networks]
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

(rf/reg-sub
 :wallet/transactions
 :<- [:wallet]
 :-> :transactions)

(rf/reg-sub
 :wallet/search-address
 :<- [:wallet/ui]
 :-> :search-address)

(rf/reg-sub
 :wallet/local-suggestions
 :<- [:wallet/search-address]
 :-> :local-suggestions)

(rf/reg-sub
 :wallet/valid-ens-or-address?
 :<- [:wallet/search-address]
 :-> :valid-ens-or-address?)

(rf/reg-sub
 :wallet/aggregated-fiat-balance-per-chain
 :<- [:wallet/aggregated-tokens]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[aggregated-tokens currency currency-symbol]]
   (utils/calculate-balances-per-chain
    {:tokens          aggregated-tokens
     :currency        currency
     :currency-symbol currency-symbol})))

(rf/reg-sub
 :wallet/current-viewing-account-fiat-balance-per-chain
 :<- [:wallet/current-viewing-account]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[{:keys [tokens]} currency currency-symbol]]
   (utils/calculate-balances-per-chain
    {:tokens          tokens
     :currency        currency
     :currency-symbol currency-symbol})))
