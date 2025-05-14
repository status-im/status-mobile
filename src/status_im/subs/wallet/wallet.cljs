(ns status-im.subs.wallet.wallet
  (:require [cljs-time.core :as t]
            [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.contexts.wallet.common.utils.networks :as network-utils]
            [status-im.contexts.wallet.networks.core :as networks]
            [status-im.contexts.wallet.send.utils :as send-utils]
            [status-im.contexts.wallet.sheets.missing-keypair.view :as missing-keypair]
            [status-im.subs.wallet.add-account.address-to-watch]
            [utils.collection]
            [utils.money :as money]
            [utils.number]
            [utils.security.core :as security]))

(rf/reg-sub
 :wallet/ui
 :<- [:wallet]
 :-> :ui)

(rf/reg-sub
 :wallet/scanned-address
 :<- [:wallet/ui]
 :-> :scanned-address)

(rf/reg-sub
 :wallet/last-updates-per-address
 :<- [:wallet/ui]
 :-> :last-updates-per-address)

(rf/reg-sub
 :wallet/latest-update
 :<- [:wallet/last-updates-per-address]
 (fn [last-updates-per-address]
   (->> last-updates-per-address
        vals
        (reduce (fn [earliest-time last-update-time]
                  (if (or (nil? earliest-time)
                          (t/before? last-update-time earliest-time))
                    last-update-time
                    earliest-time))))))

(rf/reg-sub
 :wallet/blockchain
 :<- [:wallet]
 :-> :blockchain)

(rf/reg-sub
 :wallet/blockchain-status
 :<- [:wallet/blockchain]
 :-> :status)

(rf/reg-sub
 :wallet/tokens
 :<- [:wallet]
 :-> :tokens)

(rf/reg-sub
 :wallet/tokens-by-symbol
 :<- [:wallet/tokens]
 (fn [{:keys [by-symbol]}]
   (utils.collection/index-by :symbol by-symbol)))

(rf/reg-sub
 :wallet/prices-per-token
 :<- [:wallet/tokens]
 :-> :prices-per-token)

(rf/reg-sub
 :wallet/market-values-per-token
 :<- [:wallet/tokens]
 :-> :market-values-per-token)

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
 :wallet/current-viewing-account-address
 :<- [:wallet]
 :-> :current-viewing-account-address)

(rf/reg-sub
 :wallet/wallet-send-to-address
 :<- [:wallet/wallet-send]
 :-> :to-address)

(rf/reg-sub
 :wallet/wallet-send-receiver-networks
 :<- [:wallet/wallet-send]
 :-> :receiver-networks)

(rf/reg-sub
 :wallet/wallet-send-route
 :<- [:wallet/wallet-send]
 :-> :route)

(rf/reg-sub
 :wallet/send-best-route
 :<- [:wallet/wallet-send-route]
 (fn [routes]
   (first routes)))

(rf/reg-sub
 :wallet/suggested-gas-fees-for-setting
 :<- [:wallet/route]
 (fn [route]
   (:fees-by-mode route)))

(rf/reg-sub
 :wallet/wallet-send-enough-assets?
 :<- [:wallet/wallet-send]
 :-> :enough-assets?)

(rf/reg-sub
 :wallet/wallet-send-collectible
 :<- [:wallet/wallet-send]
 :-> :collectible)

(rf/reg-sub
 :wallet/wallet-send-token
 :<- [:wallet/wallet-send]
 :<- [:wallet/active-networks]
 (fn [[wallet-send networks]]
   (let [token                  (:token wallet-send)
         enabled-from-chain-ids (->> networks
                                     (map :chain-id)
                                     set)]
     (some-> token
             (assoc :networks           (network-utils/network-list-with-positive-balance token networks)
                    :supported-networks (network-utils/network-list token networks)
                    :available-balance  (utils/calculate-total-token-balance token)
                    :total-balance      (utils/calculate-total-token-balance
                                         token
                                         enabled-from-chain-ids))))))

(rf/reg-sub
 :wallet/bridge-token
 :<- [:wallet/wallet-send-token]
 (fn [token]
   (update token :networks #(filter networks/bridge-supported-network? %))))

(rf/reg-sub
 :wallet/wallet-send-token-symbol
 :<- [:wallet/wallet-send]
 (fn [{:keys [token-symbol token]}]
   (or token-symbol (:symbol token))))

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
 :wallet/send-tx-type-collectible?
 :<- [:wallet/wallet-send-tx-type]
 (fn [tx-type]
   (send-utils/tx-type-collectible? tx-type)))

(rf/reg-sub
 :wallet/send-general-flow?
 :<- [:wallet/wallet-send]
 :-> :general-flow?)

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
 :wallet/selected-keypair-keycard?
 :<- [:wallet/selected-keypair]
 (fn [{:keys [keycards]}]
   (boolean (seq keycards))))

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
    (fn [acc {:keys [accounts name type key-uid lowest-operability keycards]}]
      (if (= lowest-operability :no)
        (update acc
                :missing
                conj
                {:type     type
                 :keycard? (boolean (seq keycards))
                 :name     name
                 :key-uid  key-uid
                 :accounts (format-settings-missing-keypair-accounts accounts)})
        (update acc
                :operable
                conj
                {:type     type
                 :keycard? (boolean (seq keycards))
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
 :wallet/accounts-by-address
 :<- [:wallet]
 :-> :accounts)

(rf/reg-sub
 :wallet/accounts
 :<- [:wallet/accounts-by-address]
 (fn [accounts-by-address]
   (->> accounts-by-address
        vals
        (sort-by :position))))

(rf/reg-sub
 :wallet/accounts-without-assets
 :<- [:wallet/accounts]
 (fn [accounts]
   (map #(dissoc % :tokens :collectibles) accounts)))

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
 :wallet/balances-in-active-networks
 :<- [:wallet/accounts]
 :<- [:profile/currency]
 :<- [:wallet/filtered-chain-ids]
 :<- [:wallet/prices-per-token]
 (fn [[accounts currency chain-ids prices-per-token]]
   (zipmap (map :address accounts)
           (map (fn [account]
                  (utils/calculate-balance-from-tokens {:currency         currency
                                                        :tokens           (:tokens account)
                                                        :chain-ids        chain-ids
                                                        :prices-per-token prices-per-token}))
                accounts))))

(rf/reg-sub
 :wallet/balances-by-network
 :<- [:wallet/operable-accounts]
 :<- [:profile/currency]
 :<- [:wallet/active-chain-ids]
 :<- [:wallet/prices-per-token]
 (fn [[accounts currency chain-ids prices-per-token]]
   (zipmap chain-ids
           (map (fn [chain-id]
                  (->> accounts
                       (reduce (fn [acc {:keys [tokens]}]
                                 (-> (utils/calculate-balance-from-tokens {:currency currency
                                                                           :tokens tokens
                                                                           :chain-ids [chain-id]
                                                                           :prices-per-token
                                                                           prices-per-token})
                                     (money/add acc)))
                               (money/bignumber 0))))
                chain-ids))))

(rf/reg-sub
 :wallet/current-account-balances-by-network
 :<- [:wallet/current-viewing-account]
 :<- [:profile/currency]
 :<- [:wallet/active-chain-ids]
 :<- [:wallet/prices-per-token]
 (fn [[current-account currency chain-ids prices-per-token]]
   (zipmap chain-ids
           (map (fn [chain-id]
                  (utils/calculate-balance-from-tokens {:currency         currency
                                                        :tokens           (:tokens current-account)
                                                        :chain-ids        [chain-id]
                                                        :prices-per-token prices-per-token}))
                chain-ids))))

(rf/reg-sub
 :wallet/account-cards-data
 :<- [:wallet/accounts]
 :<- [:wallet/balances-in-active-networks]
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
 :<- [:wallet/balances-in-active-networks]
 :<- [:profile/currency-symbol]
 (fn [[accounts current-viewing-account-address balances currency-symbol]]
   (let [balance           (get balances current-viewing-account-address)
         formatted-balance (utils/prettify-balance currency-symbol balance)]
     (-> accounts
         (utils/get-account-by-address current-viewing-account-address)
         (assoc :balance           balance
                :formatted-balance formatted-balance)))))

(rf/reg-sub
 :wallet/current-viewing-account-or-default
 :<- [:wallet/accounts]
 :<- [:wallet/current-viewing-account-address]
 :<- [:wallet/balances-in-active-networks]
 :<- [:profile/currency-symbol]
 (fn [[accounts current-viewing-account-address balances currency-symbol]]
   (let [account           (or (utils/get-account-by-address accounts current-viewing-account-address)
                               (utils/get-default-account accounts))
         address           (:address account)
         balance           (get balances address)
         formatted-balance (utils/prettify-balance currency-symbol balance)]
     (assoc account
            :balance           balance
            :formatted-balance formatted-balance))))

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
 :wallet/current-viewing-account-tokens-in-active-networks
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/filtered-chain-ids]
 (fn [[{:keys [tokens]} chain-ids]]
   (utils/filter-tokens-in-chains tokens chain-ids)))

(rf/reg-sub
 :wallet/current-viewing-account-tokens-filtered
 :<- [:wallet/current-viewing-account-or-default]
 :<- [:wallet/active-networks]
 :<- [:wallet/wallet-send]
 :<- [:profile/currency]
 :<- [:wallet/prices-per-token]
 (fn [[account networks send-data currency price-per-token] [_ {:keys [query chain-ids hide-token-fn]}]]
   (let [tx-type       (:tx-type send-data)
         tokens        (->> (:tokens account)
                            (map
                             (fn [token]
                               (let [total-balance (utils/calculate-total-token-balance
                                                    token
                                                    chain-ids)]
                                 (assoc token
                                        :disabled? (and (= tx-type :tx/bridge)
                                                        (send-utils/bridge-disabled? (:symbol
                                                                                      token)))
                                        :networks (cond->>
                                                    (network-utils/network-list-with-positive-balance
                                                     token
                                                     networks)
                                                    chain-ids
                                                    (filter #(some #{(:chain-id %)} chain-ids)))
                                        :supported-networks (network-utils/network-list token networks)
                                        :available-balance (utils/calculate-total-token-balance token)
                                        :total-balance total-balance
                                        :fiat-value (utils/calculate-token-fiat-value
                                                     {:currency         currency
                                                      :balance          total-balance
                                                      :token            token
                                                      :prices-per-token price-per-token})))))
                            (filter (fn [{:keys [networks]}]
                                      (pos? (count networks))))
                            (remove #(when hide-token-fn (hide-token-fn constants/swap-tokens-my %))))
         sorted-tokens (utils/sort-tokens-by-fiat-value tokens)]
     (if query
       (let [query-string (string/lower-case query)]
         (filter #(or (string/starts-with? (string/lower-case (:name %)) query-string)
                      (string/starts-with? (string/lower-case (:symbol %)) query-string))
                 sorted-tokens))
       sorted-tokens))))


(rf/reg-sub
 :wallet/tokens-filtered
 :<- [:wallet/tokens]
 (fn [{:keys [by-symbol market-values-per-token details-per-token]}
      [_ {:keys [query chain-ids hide-token-fn]}]]
   (let [tokens        (->> by-symbol
                            (map (fn [token]
                                   (let [token-symbol (keyword (:symbol token))]
                                     (-> token
                                         (assoc :market-values
                                                (get market-values-per-token token-symbol))
                                         (assoc :details (get details-per-token token-symbol))))))
                            (filter (fn [{:keys [chain-id]}]
                                      (some #{chain-id} chain-ids)))
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
 :<- [:wallet/current-viewing-account-or-default]
 :<- [:wallet/networks]
 (fn [[{:keys [tokens]} networks] [_ token-symbol chain-ids]]
   (->> (utils/tokens-with-balance tokens networks chain-ids)
        (filter #(= (string/lower-case (:symbol %))
                    (string/lower-case token-symbol)))
        first)))

(rf/reg-sub
 :wallet/token-by-symbol-from-first-available-account-with-balance
 :<- [:wallet/operable-accounts]
 :<- [:wallet/current-viewing-account-or-default]
 :<- [:wallet/networks]
 (fn [[accounts {:keys [tokens]} networks] [_ token-symbol chain-ids]]
   (when token-symbol
     (or
      (->> (utils/tokens-with-balance tokens networks chain-ids)
           (filter #(and (= (string/lower-case (:symbol %))
                            (string/lower-case token-symbol))
                         (money/greater-than (:total-balance %) 0)))
           first)
      (some
       (fn [{:keys [tokens]}]
         (->> (utils/tokens-with-balance tokens networks chain-ids)
              (filter #(and (= (string/lower-case (:symbol %))
                               (string/lower-case token-symbol))
                            (money/greater-than (:total-balance %) 0)))
              first))
       accounts)))))

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
 :wallet/operable-addresses-with-token-symbol
 :<- [:wallet/operable-addresses-tokens-with-positive-balance]
 (fn [addresses-tokens [_ token-symbol]]
   (keep (fn [[address tokens]]
           (some #(when (= (:symbol %) token-symbol) address)
                 tokens))
         addresses-tokens)))

(rf/reg-sub
 :wallet/current-account-owns-token
 (fn [[_ token-symbol]]
   [(rf/subscribe [:wallet/current-viewing-account-address])
    (rf/subscribe [:wallet/operable-addresses-with-token-symbol token-symbol])])
 (fn [[address addresses-with-token]]
   (-> addresses-with-token set (contains? address))))

(rf/reg-sub
 :wallet/account-tab
 :<- [:wallet/ui]
 (fn [ui]
   (get-in ui [:account-page :active-tab])))

(rf/reg-sub
 :wallet/home-tab
 :<- [:wallet/ui]
 (fn [ui]
   (:active-tab ui)))

(rf/reg-sub
 :wallet/aggregated-tokens
 :<- [:wallet/accounts-without-watched-accounts]
 (fn [accounts]
   (utils/aggregate-tokens-for-all-accounts accounts)))

(rf/reg-sub
 :wallet/aggregated-tokens-in-active-networks
 :<- [:wallet/aggregated-tokens]
 :<- [:wallet/filtered-chain-ids]
 (fn [[aggregated-tokens chain-ids]]
   (utils/filter-tokens-in-chains aggregated-tokens chain-ids)))

(rf/reg-sub
 :wallet/current-viewing-account-token-values
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/current-viewing-account-tokens-in-active-networks]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 :<- [:wallet/prices-per-token]
 :<- [:wallet/market-values-per-token]
 (fn [[{:keys [color]} tokens currency currency-symbol prices-per-token market-values-per-token]]
   (utils/calculate-and-sort-tokens {:tokens                  tokens
                                     :color                   color
                                     :currency                currency
                                     :currency-symbol         currency-symbol
                                     :prices-per-token        prices-per-token
                                     :market-values-per-token market-values-per-token})))

(rf/reg-sub
 :wallet/aggregated-token-values-and-balance
 :<- [:wallet/aggregated-tokens-in-active-networks]
 :<- [:profile/customization-color]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 :<- [:wallet/prices-per-token]
 :<- [:wallet/market-values-per-token]
 (fn [[aggregated-tokens color currency currency-symbol prices-per-token market-values-per-token]]
   (let [balance             (utils/calculate-balance-from-tokens {:currency         currency
                                                                   :tokens           aggregated-tokens
                                                                   :prices-per-token prices-per-token})
         formatted-balance   (utils/prettify-balance currency-symbol balance)
         sorted-token-values (utils/calculate-and-sort-tokens {:tokens aggregated-tokens
                                                               :color color
                                                               :currency currency
                                                               :currency-symbol currency-symbol
                                                               :prices-per-token prices-per-token
                                                               :market-values-per-token
                                                               market-values-per-token})]
     {:balance           balance
      :formatted-balance formatted-balance
      :tokens            sorted-token-values})))

(rf/reg-sub
 :wallet/zero-balance-in-all-non-watched-accounts?
 :<- [:wallet/aggregated-tokens]
 (fn [aggregated-tokens]
   (let [zero-balance? (->> aggregated-tokens
                            (mapcat (comp vals :balances-per-chain))
                            (every? #(money/equal-to (:raw-balance %) 0)))]
     (and (not-empty aggregated-tokens) zero-balance?))))

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
 :wallet/wallet-send-fee-fiat-formatted
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/wallet-send-route]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 :<- [:wallet/prices-per-token]
 (fn [[account route currency currency-symbol prices-per-token]]
   (let [token-symbol-for-fees (get-in (first route) [:from :native-currency-symbol])]
     (when token-symbol-for-fees
       (let [tokens              (:tokens account)
             token-for-fees      (first (filter #(= (string/lower-case (:symbol %))
                                                    (string/lower-case token-symbol-for-fees))
                                                tokens))
             fee-in-native-token (send-utils/full-route-gas-fee route)
             fee-in-fiat         (utils/calculate-token-fiat-value
                                  {:currency         currency
                                   :balance          fee-in-native-token
                                   :token            token-for-fees
                                   :prices-per-token prices-per-token})
             fee-formatted       (utils/fiat-formatted-for-ui
                                  currency-symbol
                                  fee-in-fiat)]
         fee-formatted)))))

(rf/reg-sub
 :wallet/wallet-transaction-setting-fiat-formatted
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/route]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 :<- [:wallet/prices-per-token]
 :<- [:wallet/suggested-gas-fees-for-setting]
 (fn [[account route currency currency-symbol prices-per-token fees-for-settings]
      [_ transaction-setting]]
   (let [token-symbol-for-fees (get-in route [:from :native-currency-symbol])]
     (when token-symbol-for-fees
       (let [tokens              (:tokens account)
             token-for-fees      (first (filter #(= (string/lower-case (:symbol %))
                                                    (string/lower-case token-symbol-for-fees))
                                                tokens))
             gas-price           (-> fees-for-settings
                                     (get transaction-setting)
                                     :max-fees)
             fee-in-native-token (send-utils/full-route-gas-fee-for-custom-gas-price route gas-price)
             fee-in-fiat         (utils/calculate-token-fiat-value
                                  {:currency         currency
                                   :balance          fee-in-native-token
                                   :token            token-for-fees
                                   :prices-per-token prices-per-token})
             fee-formatted       (utils/fiat-formatted-for-ui
                                  currency-symbol
                                  fee-in-fiat)]
         fee-formatted)))))

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

(rf/reg-sub
 :wallet/accounts-with-balances
 :<- [:wallet/operable-accounts]
 (fn [accounts [_ token]]
   (let [token-symbol (:symbol token)]
     (map
      (fn [account]
        (let [tokens            (:tokens account)
              filtered-tokens   (filter #(= (:symbol %) token-symbol) tokens)
              asset-pay-balance (utils/calculate-total-token-balance filtered-tokens)]
          (assoc account
                 :asset-pay-balance (utils/sanitized-token-amount-to-display
                                     asset-pay-balance
                                     constants/min-token-decimals-to-display)
                 :asset-pay-symbol  token-symbol)))
      accounts))))

(rf/reg-sub
 :wallet/route
 :<- [:wallet/send-tx-type]
 :<- [:wallet/send-route]
 :<- [:wallet/swap-proposal]
 (fn [[tx-type send-route swap-proposal]]
   (if tx-type
     (first send-route)
     swap-proposal)))

(rf/reg-sub
 :wallet/user-fee-mode-settings
 :<- [:wallet/ui]
 :-> :user-fee-mode)

(rf/reg-sub
 :wallet/user-tx-settings
 :<- [:wallet/ui]
 :-> :user-tx-settings)

(rf/reg-sub
 :wallet/tx-settings-max-base-fee-user
 :<- [:wallet/user-tx-settings]
 :-> :max-base-fee)

(rf/reg-sub
 :wallet/tx-settings-priority-fee-user
 :<- [:wallet/user-tx-settings]
 :-> :priority-fee)

(rf/reg-sub
 :wallet/tx-settings-nonce-user
 :<- [:wallet/user-tx-settings]
 :-> :nonce)

(rf/reg-sub
 :wallet/tx-settings-gas-amount-user
 :<- [:wallet/user-tx-settings]
 :-> :gas-amount)

(rf/reg-sub
 :wallet/tx-settings-gas-fees
 :<- [:wallet/route]
 (fn [route]
   (:gas-fees route)))

(rf/reg-sub
 :wallet/tx-settings-max-base-fee-route
 :<- [:wallet/tx-settings-gas-fees]
 (fn [gas-fees]
   (:base-fee gas-fees)))

(rf/reg-sub
 :wallet/tx-settings-network-base-fee-route
 :<- [:wallet/tx-settings-gas-fees]
 (fn [gas-fees]
   (:network-base-fee gas-fees)))

(rf/reg-sub
 :wallet/tx-settings-gas-amount-route
 :<- [:wallet/route]
 (fn [route]
   (:gas-amount route)))

(rf/reg-sub
 :wallet/tx-settings-suggested-tx-gas-amount
 :<- [:wallet/route]
 (fn [route]
   (:suggested-tx-gas-amount route)))

(rf/reg-sub
 :wallet/tx-settings-fee-mode
 :<- [:wallet/route]
 :<- [:wallet/user-fee-mode-settings]
 (fn [[route value-set-by-user]]
   (or (:tx-fee-mode route) value-set-by-user)))

(rf/reg-sub
 :wallet/tx-settings-max-base-fee
 :<- [:wallet/tx-settings-max-base-fee-route]
 :<- [:wallet/tx-settings-max-base-fee-user]
 (fn [[value-from-routes value-set-by-user]]
   (or value-set-by-user value-from-routes)))

(rf/reg-sub
 :wallet/tx-settings-priority-fee
 :<- [:wallet/tx-settings-gas-fees]
 :<- [:wallet/tx-settings-priority-fee-user]
 (fn [[gas-fees value-set-by-user]]
   (or value-set-by-user (:tx-priority-fee gas-fees))))

(rf/reg-sub
 :wallet/tx-settings-custom-priority-fee
 :<- [:wallet/tx-settings-fee-mode]
 :<- [:wallet/tx-settings-gas-fees]
 :<- [:wallet/tx-settings-priority-fee-user]
 :<- [:wallet/tx-settings-suggested-min-priority-fee]
 (fn [[fee-mode gas-fees value-set-by-user min-priority-fee]]
   (cond
     value-set-by-user                value-set-by-user
     (= :tx-fee-mode/custom fee-mode) (:tx-priority-fee gas-fees)
     :else                            min-priority-fee)))

(rf/reg-sub
 :wallet/tx-settings-gas-amount
 :<- [:wallet/tx-settings-gas-amount-route]
 :<- [:wallet/tx-settings-gas-amount-user]
 (fn [[value-from-routes value-set-by-user]]
   (or value-set-by-user value-from-routes)))

(rf/reg-sub
 :wallet/tx-settings-nonce
 :<- [:wallet/route]
 :<- [:wallet/tx-settings-nonce-user]
 (fn [[route value-set-by-user]]
   (or value-set-by-user (:nonce route))))

(rf/reg-sub
 :wallet/tx-settings-suggested-nonce
 :<- [:wallet/route]
 (fn [route]
   (:suggested-tx-nonce route)))

(rf/reg-sub
 :wallet/tx-settings-suggested-max-priority-fee
 :<- [:wallet/tx-settings-gas-fees]
 (fn [gas-fees]
   (:suggested-max-priority-fee gas-fees)))

(rf/reg-sub
 :wallet/tx-settings-suggested-min-priority-fee
 :<- [:wallet/tx-settings-gas-fees]
 (fn [gas-fees]
   (:suggested-min-priority-fee gas-fees)))
