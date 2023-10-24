(ns status-im2.data-store.wallet
  (:require
    clojure.set))

(defn <-rpc
  [network]
  (-> network
      (clojure.set/rename-keys
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
