(ns status-im.subs.wallet.wallet
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.contexts.wallet.common.utils.networks :as network-utils]
            [status-im.contexts.wallet.send.utils :as send-utils]
            [status-im.contexts.wallet.sheets.missing-keypair.view :as missing-keypair]
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
 :wallet/tokens
 :<- [:wallet]
 :-> :tokens)

(rf/reg-sub
 :wallet/tokens-loading
 :<- [:wallet/ui]
 :-> :tokens-loading)

(rf/reg-sub
 :wallet/home-tokens-loading?
 :<- [:wallet/tokens-loading]
 (fn [tokens-loading]
   (->> tokens-loading
        vals
        (some true?)
        boolean)))

(rf/reg-sub
 :wallet/current-viewing-account-tokens-loading?
 :<- [:wallet/tokens-loading]
 :<- [:wallet/current-viewing-account-address]
 (fn [[tokens-loading current-viewing-account-address]]
   (get tokens-loading current-viewing-account-address)))

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
     (some-> token
             (assoc :networks          (network-utils/network-list token networks)
                    :available-balance (utils/calculate-total-token-balance token)
                    :total-balance     (utils/calculate-total-token-balance
                                        token
                                        enabled-from-chain-ids))))))

(rf/reg-sub
 :wallet/wallet-send-token-symbol
 :<- [:wallet/wallet-send]
 :-> :token-symbol)

(rf/reg-sub
 :wallet/wallet-send-disabled-from-chain-ids
 :<- [:wallet/wallet-send]
 :-> :disabled-from-chain-ids)

(rf/reg-sub
 :wallet/wallet-send-from-locked-amounts
 :<- [:wallet/wallet-send]
 :-> :from-locked-amounts)

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
 :wallet/keypairs-list
 :<- [:wallet]
 (fn [{:keys [keypairs]}]
   (vals keypairs)))

(rf/reg-sub
 :wallet/fully-operable-keypairs-list
 :<- [:wallet/keypairs-list]
 (fn [keypairs]
   (filter #(and (= :fully (:lowest-operability %)) (not-empty (:derived-from %))) keypairs)))

(rf/reg-sub
 :wallet/keypair-names
 :<- [:wallet/keypairs-list]
 (fn [keypairs]
   (set (map :name keypairs))))

(rf/reg-sub
 :wallet/selected-keypair-uid
 :<- [:wallet/create-account]
 :-> :selected-keypair-uid)

(rf/reg-sub
 :wallet/selected-keypair
 :<- [:wallet/keypairs]
 :<- [:wallet/selected-keypair-uid]
 (fn [[keypairs selected-keypair-uid]]
   (get keypairs selected-keypair-uid)))

(rf/reg-sub
 :wallet/selected-primary-keypair?
 :<- [:wallet/keypairs]
 :<- [:wallet/selected-keypair-uid]
 (fn [[keypairs selected-keypair-uid]]
   (= (get-in keypairs [selected-keypair-uid :type])
      :profile)))

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
       (keep (fn [{:keys [path color emoji name address]}]
               (when-not (string/starts-with? (str path) constants/path-eip1581)
                 {:account-props {:customization-color color
                                  :size                size
                                  :emoji               emoji
                                  :type                :default
                                  :name                name
                                  :address             address}
                  :networks      networks
                  :state         :default
                  :blur?         true
                  :action        :none})))))

(defn- format-settings-missing-keypair-accounts
  [accounts]
  (->> accounts
       (map (fn [{:keys [color emoji]}]
              {:customization-color color
               :emoji               emoji
               :type                :default}))))

(rf/reg-sub
 :wallet/settings-keypairs-accounts
 :<- [:wallet/keypairs-list]
 (fn [keypairs [_ format-options]]
   (reduce
    (fn [acc {:keys [accounts name type key-uid lowest-operability]}]
      (if (= lowest-operability :no)
        (update acc
                :missing
                conj
                {:type     type
                 :name     name
                 :key-uid  key-uid
                 :accounts (format-settings-missing-keypair-accounts accounts)})
        (update acc
                :operable
                conj
                {:type     type
                 :name     name
                 :key-uid  key-uid
                 :accounts (format-settings-keypair-accounts accounts format-options)})))
    {:missing  []
     :operable []}
    keypairs)))

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
 :<- [:wallet/tokens-loading]
 :<- [:network/online?]
 :<- [:profile/currency-symbol]
 :<- [:wallet/keypairs]
 (fn [[accounts balances tokens-loading online? currency-symbol keypairs]]
   (mapv (fn [{:keys [color address watch-only? key-uid operable] :as account}]
           (let [account-type (cond
                                (= operable :no) :missing-keypair
                                watch-only?      :watch-only
                                :else            :empty)
                 keypair      (get keypairs key-uid)]
             (assoc account
                    :customization-color color
                    :type                (cond
                                           (= operable :no) :missing-keypair
                                           watch-only?      :watch-only
                                           :else            :empty)
                    :on-press            (if (= account-type :missing-keypair)
                                           (fn []
                                             (rf/dispatch [:show-bottom-sheet
                                                           {:content #(missing-keypair/view
                                                                       account
                                                                       keypair)}]))
                                           #(rf/dispatch [:wallet/navigate-to-account address]))
                    :loading?            (and online?
                                              (or (get tokens-loading address)
                                                  (not (contains? tokens-loading address))))
                    :balance             (utils/prettify-balance currency-symbol
                                                                 (get balances address)))))
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
   (get keypairs key-uid)))

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
 :<- [:wallet/wallet-send]
 (fn [[account networks send-data] [_ {:keys [query chain-ids hide-token-fn]}]]
   (let [tx-type       (:tx-type send-data)
         tokens        (->> (:tokens account)
                            (map (fn [token]
                                   (assoc token
                                          :bridge-disabled?  (and (= tx-type :tx/bridge)
                                                                  (send-utils/bridge-disabled? (:symbol
                                                                                                token)))
                                          :networks          (network-utils/network-list token networks)
                                          :available-balance (utils/calculate-total-token-balance token)
                                          :total-balance     (utils/calculate-total-token-balance
                                                              token
                                                              chain-ids))))
                            (remove #(when hide-token-fn (hide-token-fn constants/swap-tokens-my %))))
         sorted-tokens (utils/sort-tokens tokens)]
     (if query
       (let [query-string (string/lower-case query)]
         (filter #(or (string/starts-with? (string/lower-case (:name %)) query-string)
                      (string/starts-with? (string/lower-case (:symbol %)) query-string))
                 sorted-tokens))
       sorted-tokens))))

(rf/reg-sub
 :wallet/tokens-filtered
 :<- [:wallet/tokens]
 (fn [{:keys [by-symbol market-values-per-token details-per-token]} [_ {:keys [query hide-token-fn]}]]
   (let [tokens        (->> by-symbol
                            (map (fn [token]
                                   (-> token
                                       (assoc :market-values
                                              (get market-values-per-token (:symbol token)))
                                       (assoc :details (get details-per-token (:symbol token))))))
                            (remove #(when hide-token-fn
                                       (hide-token-fn constants/swap-tokens-popular %))))
         sorted-tokens (utils/sort-tokens-by-name tokens)]
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
 (fn [[{:keys [tokens]} networks] [_ token-symbol chain-ids]]
   (->> (utils/tokens-with-balance tokens networks chain-ids)
        (filter #(= (string/lower-case (:symbol %))
                    (string/lower-case token-symbol)))
        first)))

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

(defn- keep-operable-accounts
  [accounts]
  (filter :operable? accounts))

(rf/reg-sub
 :wallet/operable-accounts-without-current-viewing-account
 :<- [:wallet/accounts-without-current-viewing-account]
 keep-operable-accounts)

(rf/reg-sub
 :wallet/operable-accounts
 :<- [:wallet/accounts-without-watched-accounts]
 keep-operable-accounts)

(rf/reg-sub
 :wallet/operable-addresses-tokens-with-positive-balance
 :<- [:wallet/operable-accounts]
 (fn [accounts]
   (let [positive-balance-in-any-chain? (fn [{:keys [balances-per-chain]}]
                                          (->> balances-per-chain
                                               (map (comp :raw-balance val))
                                               (some pos?)))]
     (as-> accounts $
       (group-by :address $)
       (update-vals $ #(filter positive-balance-in-any-chain? (:tokens (first %))))))))

(rf/reg-sub
 :wallet/accounts-with-current-asset
 :<- [:wallet/operable-accounts]
 :<- [:wallet/operable-addresses-tokens-with-positive-balance]
 :<- [:wallet/wallet-send-token-symbol]
 :<- [:wallet/wallet-send-token]
 (fn [[accounts addresses-tokens token-symbol token]]
   (if-let [asset-symbol (or token-symbol (:symbol token))]
     (let [addresses-with-asset (as-> addresses-tokens $
                                  (update-vals $ #(set (map :symbol %)))
                                  (keep (fn [[address token-symbols]]
                                          (when (token-symbols asset-symbol) address))
                                        $)
                                  (set $))]
       (filter #(addresses-with-asset (:address %)) accounts))
     accounts)))

(rf/reg-sub
 :wallet/operable-addresses-with-token-symbol
 :<- [:wallet/operable-addresses-tokens-with-positive-balance]
 (fn [addresses-tokens [_ token-symbol]]
   (keep (fn [[address tokens]]
           (some #(when (= (:symbol %) token-symbol) address)
                 tokens))
         addresses-tokens)))

(rf/reg-sub
 :wallet/account-tab
 :<- [:wallet/ui]
 (fn [ui]
   (get-in ui [:account-page :active-tab])))

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
 :wallet/current-viewing-account-token-values
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/current-viewing-account-tokens-in-selected-networks]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[{:keys [color]} tokens currency currency-symbol]]
   (utils/calculate-and-sort-tokens {:tokens          tokens
                                     :color           color
                                     :currency        currency
                                     :currency-symbol currency-symbol})))

(rf/reg-sub
 :wallet/aggregated-token-values-and-balance
 :<- [:wallet/aggregated-tokens-in-selected-networks]
 :<- [:profile/customization-color]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[aggregated-tokens color currency currency-symbol]]
   (let [balance             (utils/calculate-balance-from-tokens {:currency currency
                                                                   :tokens   aggregated-tokens})
         formatted-balance   (utils/prettify-balance currency-symbol balance)
         sorted-token-values (utils/calculate-and-sort-tokens {:tokens          aggregated-tokens
                                                               :color           color
                                                               :currency        currency
                                                               :currency-symbol currency-symbol})]
     {:balance           balance
      :formatted-balance formatted-balance
      :tokens            sorted-token-values})))


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
 :wallet/searching-address?
 :<- [:wallet/search-address]
 :-> :loading?)

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

(rf/reg-sub
 :wallet/wallet-send-fee-fiat-formatted
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/wallet-send-route]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[account route currency currency-symbol] [_ token-symbol-for-fees]]
   (when token-symbol-for-fees
     (let [tokens                  (:tokens account)
           token-for-fees          (first (filter #(= (string/lower-case (:symbol %))
                                                      (string/lower-case token-symbol-for-fees))
                                                  tokens))
           fee-in-native-token     (send-utils/calculate-full-route-gas-fee route)
           fee-in-crypto-formatted (utils/get-standard-crypto-format
                                    token-for-fees
                                    fee-in-native-token)
           fee-in-fiat             (utils/calculate-token-fiat-value
                                    {:currency currency
                                     :balance  fee-in-native-token
                                     :token    token-for-fees})
           fee-formatted           (utils/get-standard-fiat-format
                                    fee-in-crypto-formatted
                                    currency-symbol
                                    fee-in-fiat)]
       fee-formatted))))

(rf/reg-sub
 :wallet/has-partially-operable-accounts?
 :<- [:wallet/accounts]
 (fn [accounts]
   (->> accounts
        (some #(= :partially (:operable %)))
        boolean)))

(rf/reg-sub
 :wallet/accounts-names
 :<- [:wallet/accounts]
 (fn [accounts]
   (set (map :name accounts))))

(rf/reg-sub
 :wallet/accounts-names-without-current-account
 :<- [:wallet/accounts-names]
 :<- [:wallet/current-viewing-account]
 (fn [[account-names current-viewing-account]]
   (disj account-names (:name current-viewing-account))))

(defn- get-emoji-and-colors-from-accounts
  [accounts]
  (->> accounts
       (map (fn [{:keys [emoji color]}] [emoji color]))
       (set)))

(rf/reg-sub
 :wallet/accounts-emojis-and-colors
 :<- [:wallet/accounts]
 (fn [accounts]
   (get-emoji-and-colors-from-accounts accounts)))

(rf/reg-sub
 :wallet/accounts-emojis-and-colors-without-current-account
 :<- [:wallet/accounts-without-current-viewing-account]
 (fn [accounts]
   (get-emoji-and-colors-from-accounts accounts)))
