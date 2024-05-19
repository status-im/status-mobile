(ns status-im.subs.wallet.wallet
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.contexts.wallet.common.utils.networks :as network-utils]
            [status-im.subs.wallet.add-account.address-to-watch]
            [utils.number]
            [utils.security.core :as security]))

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
 :wallet/create-account-new-keypair
 :<- [:wallet/create-account]
 :-> :new-keypair)

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
 :wallet/wallet-send-receiver-networks
 :<- [:wallet/wallet-send]
 :-> :receiver-networks)

(rf/reg-sub
 :wallet/wallet-send-receiver-preferred-networks
 :<- [:wallet/wallet-send]
 :-> :receiver-preferred-networks)

(rf/reg-sub
 :wallet/wallet-send-route
 :<- [:wallet/wallet-send]
 :-> :route)

(rf/reg-sub
 :wallet/wallet-send-token
 :<- [:wallet/wallet-send]
 :<- [:wallet/network-details]
 :<- [:wallet/wallet-send-disabled-from-chain-ids]
 (fn [[wallet-send networks disabled-from-chain-ids]]
   (let [token                  (:token wallet-send)
         enabled-from-chain-ids (->> networks
                                     (filter #(not (contains? (set disabled-from-chain-ids)
                                                              (:chain-id %))))
                                     (map :chain-id)
                                     set)]
     (assoc token
            :networks          (network-utils/network-list token networks)
            :available-balance (utils/calculate-total-token-balance token)
            :total-balance     (utils/calculate-total-token-balance token enabled-from-chain-ids)))))

(rf/reg-sub
 :wallet/wallet-send-disabled-from-chain-ids
 :<- [:wallet/wallet-send]
 :-> :disabled-from-chain-ids)

(rf/reg-sub
 :wallet/wallet-send-from-values-by-chain
 :<- [:wallet/wallet-send]
 :-> :from-values-by-chain)

(rf/reg-sub
 :wallet/wallet-send-to-values-by-chain
 :<- [:wallet/wallet-send]
 :-> :to-values-by-chain)

(rf/reg-sub
 :wallet/wallet-send-loading-suggested-routes?
 :<- [:wallet/wallet-send]
 :-> :loading-suggested-routes?)

(rf/reg-sub
 :wallet/wallet-send-suggested-routes
 :<- [:wallet/wallet-send]
 :-> :suggested-routes)

(rf/reg-sub
 :wallet/wallet-send-sender-network-values
 :<- [:wallet/wallet-send]
 :-> :sender-network-values)

(rf/reg-sub
 :wallet/wallet-send-receiver-network-values
 :<- [:wallet/wallet-send]
 :-> :receiver-network-values)

(rf/reg-sub
 :wallet/wallet-send-network-links
 :<- [:wallet/wallet-send]
 :-> :network-links)

(rf/reg-sub
 :wallet/wallet-send-tx-type
 :<- [:wallet/wallet-send]
 :-> :tx-type)

(rf/reg-sub
 :wallet/keypairs
 :<- [:wallet]
 :-> :keypairs)

(rf/reg-sub
 :wallet/selected-keypair-uid
 :<- [:wallet/create-account]
 :-> :selected-keypair-uid)

(rf/reg-sub
 :wallet/selected-keypair
 :<- [:wallet/keypairs]
 :<- [:wallet/selected-keypair-uid]
 (fn [[keypairs selected-keypair-uid]]
   (some #(when (= (:key-uid %) selected-keypair-uid)
            %)
         keypairs)))

(rf/reg-sub
 :wallet/selected-primary-keypair?
 :<- [:wallet/keypairs]
 :<- [:wallet/selected-keypair-uid]
 (fn [[keypairs selected-keypair-uid]]
   (let [primary-keypair-uid (->> keypairs
                                  (some #(when (= (:type %) "profile") %))
                                  (:key-uid))]
     (= selected-keypair-uid primary-keypair-uid))))

(rf/reg-sub
 :wallet/selected-networks->chain-ids
 :<- [:wallet/selected-networks]
 :<- [:profile/test-networks-enabled?]
 :<- [:profile/is-goerli-enabled?]
 (fn [[selected-networks testnet-enabled? goerli-enabled?]]
   (set (map #(network-utils/network->chain-id
               {:network          %
                :testnet-enabled? testnet-enabled?
                :goerli-enabled?  goerli-enabled?})
             selected-networks))))

(defn- format-settings-keypair-accounts
  [accounts
   {:keys [networks size]
    :or   {networks []
           size     32}}]
  (->> accounts
       (keep (fn [{:keys [path customization-color emoji name address]}]
               (when-not (string/starts-with? path constants/path-eip1581)
                 {:account-props {:customization-color customization-color
                                  :size                size
                                  :emoji               emoji
                                  :type                :default
                                  :name                name
                                  :address             address}
                  :networks      networks
                  :state         :default
                  :action        :none})))))

(defn- format-settings-missing-keypair-accounts
  [accounts]
  (->> accounts
       (map (fn [{:keys [customization-color emoji]}]
              {:customization-color customization-color
               :emoji               emoji
               :type                :default}))))

(rf/reg-sub
 :wallet/settings-keypairs-accounts
 :<- [:wallet/keypairs]
 :<- [:wallet/accounts]
 (fn [[keypairs accounts] [_ format-options]]
   (let [grouped-accounts      (->> accounts
                                    (map #(select-keys % [:operable :key-uid]))
                                    (group-by :operable))
         operable-key-pair-ids (->> (map :key-uid (:fully grouped-accounts))
                                    (into #{}))
         missing-key-pair-ids  (->> (map :key-uid (:no grouped-accounts))
                                    (into #{}))]
     {:operable (->> keypairs
                     (filter #(contains? operable-key-pair-ids (:key-uid %)))
                     (map (fn [{:keys [accounts name type key-uid]}]
                            {:type     (keyword type)
                             :name     name
                             :key-uid  key-uid
                             :accounts (format-settings-keypair-accounts accounts format-options)})))
      :missing  (->> keypairs
                     (filter #(contains? missing-key-pair-ids (:key-uid %)))
                     (map (fn [{:keys [accounts name type key-uid]}]
                            {:type     (keyword type)
                             :name     name
                             :key-uid  key-uid
                             :accounts (format-settings-missing-keypair-accounts accounts)})))})))
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
 :wallet/current-viewing-account-color
 :<- [:wallet/current-viewing-account]
 :-> :color)

(rf/reg-sub
 :wallet/current-viewing-account-keypair
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/keypairs]
 (fn [[{:keys [key-uid]} keypairs]]
   (first (filter #(= key-uid (:key-uid %)) keypairs))))

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
 (fn [[account networks] [_ query chain-ids]]
   (let [tokens        (map (fn [token]
                              (assoc token
                                     :networks          (network-utils/network-list token networks)
                                     :available-balance (utils/calculate-total-token-balance token)
                                     :total-balance     (utils/calculate-total-token-balance token
                                                                                             chain-ids)))
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
 (fn [[account networks] [_ token-symbol chain-ids]]
   (let [tokens (map (fn [token]
                       (assoc token
                              :networks          (network-utils/network-list token networks)
                              :available-balance (utils/calculate-total-token-balance token)
                              :total-balance     (utils/calculate-total-token-balance token
                                                                                      chain-ids)))
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
 :<- [:wallet/accounts-without-watched-accounts]
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
 :wallet/preferred-chains-for-address
 :<- [:wallet/accounts]
 :<- [:wallet/network-details]
 :<- [:profile/test-networks-enabled?]
 (fn [[accounts network-details test-networks-enabled?] [_ address]]
   (let [preferred-chains-ids (some #(when (= (:address %) address)
                                       (if test-networks-enabled?
                                         (:test-preferred-chain-ids %)
                                         (:prod-preferred-chain-ids %)))
                                    accounts)]
     (filter #(preferred-chains-ids (:chain-id %)) network-details))))

(rf/reg-sub
 :wallet/preferred-chain-names-for-address
 (fn [[_ address]]
   (rf/subscribe [:wallet/preferred-chains-for-address address]))
 (fn [preferred-chains-for-address _]
   (map :network-name preferred-chains-for-address)))

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
 :wallet/local-suggestions->full-address
 :<- [:wallet/local-suggestions]
 (fn [local-suggestions]
   (:full-address (first local-suggestions))))

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

(rf/reg-sub
 :wallet/import-private-key
 :<- [:wallet/create-account]
 (fn [create-account]
   (some-> create-account
           :private-key
           security/unmask)))

(rf/reg-sub
 :wallet/public-address
 :<- [:wallet/create-account]
 :-> :public-address)

(rf/reg-sub
 :wallet/wallet-send-enabled-networks
 :<- [:wallet/wallet-send-token]
 :<- [:wallet/wallet-send-disabled-from-chain-ids]
 (fn [[{:keys [networks]} disabled-from-chain-ids]]
   (->> networks
        (filter #(not (contains? (set disabled-from-chain-ids)
                                 (:chain-id %))))
        set)))

(rf/reg-sub
 :wallet/wallet-send-enabled-from-chain-ids
 :<- [:wallet/wallet-send-enabled-networks]
 (fn [send-enabled-networks]
   (map :chain-id send-enabled-networks)))
