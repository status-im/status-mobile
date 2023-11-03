(ns status-im2.common.data-store.wallet
  (:require
    [clojure.set :as set]
    [clojure.string :as string]
    [status-im2.constants :as constants]))

(defn chain-ids-string->set
  [ids-string]
  (->> (string/split ids-string constants/chain-id-separator)
       (map js/parseInt)
       (into #{})))

(defn chain-ids-set->string
  [ids]
  (string/join constants/chain-id-separator ids))

(defn rpc->account
  [{:keys [colorId] :as account}]
  (-> account
      (set/rename-keys {:prodPreferredChainIds :prod-preferred-chain-ids
                        :testPreferredChainIds :test-preferred-chain-ids
                        :createdAt             :created-at})
      (update :prod-preferred-chain-ids chain-ids-string->set)
      (update :test-preferred-chain-ids chain-ids-string->set)
      (update :type keyword)
      (assoc :customization-color
             (if (seq colorId) (keyword colorId) constants/account-default-customization-color))
      (dissoc :colorId)))

(defn rpc->accounts
  [accounts]
  (->> (filter #(not (:chat %)) accounts)
       (sort-by :position)
       (map rpc->account)))

(defn <-account
  [{:keys [customization-color] :as account}]
  (-> account
      (set/rename-keys {:prod-preferred-chain-ids :prodPreferredChainIds
                        :test-preferred-chain-ids :testPreferredChainIds})
      (update :prodPreferredChainIds chain-ids-set->string)
      (update :testPreferredChainIds chain-ids-set->string)
      (assoc :colorId customization-color)
      (dissoc :customization-color)))

(defn <-rpc
  [network]
  (-> network
      (set/rename-keys
       {:Prod                   :prod
        :Test                   :test
        :isTest                 :test?
        :tokenOverrides         :token-overrides
        :rpcUrl                 :rpc-url
        :chainColor             :chain-color
        :chainName              :chain-name
        :nativeCurrencyDecimals :native-currency-decimals
        :relatedChainId         :related-chain-id
        :shortName              :short-name
        :chainId                :chain-id
        :originalFallbackURL    :original-fallback-url
        :originalRpcUrl         :original-rpc-url
        :fallbackURL            :fallback-url
        :blockExplorerUrl       :block-explorer-url
        :nativeCurrencySymbol   :native-currency-symbol
        :nativeCurrencyName     :native-currency-symbol})))
