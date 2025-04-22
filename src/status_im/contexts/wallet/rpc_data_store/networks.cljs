(ns status-im.contexts.wallet.rpc-data-store.networks
  (:require [schema.core :as schema]
            [status-im.contexts.wallet.networks.config :as networks.config]
            [status-im.contexts.wallet.rpc-data-store.utils :as utils]))

(defn- make-network
  [network-data]
  (-> (utils/extract-and-rename
       network-data
       {:isTest                 :testnet?
        :chainId                :chain-id
        :relatedChainId         :related-chain-id
        :layer                  :layer
        :shortName              :short-name
        :chainColor             :chain-color
        :chainName              :full-name
        :blockExplorerUrl       :block-explorer-url
        :nativeCurrencySymbol   :native-currency-symbol
        :nativeCurrencyName     :native-currency-name
        :nativeCurrencyDecimals :native-currency-decimals})
      (merge (get networks.config/networks (:chainId network-data)))))

(schema/=> make-network
  [:=>
   [:cat
    [:map
     [:isTest boolean?]
     [:chainId int?]
     [:relatedChainId int?]
     [:layer [:enum 1 2]]
     [:shortName string?]
     [:chainColor string?]
     [:chainName string?]
     [:blockExplorerUrl string?]
     [:nativeCurrencySymbol string?]
     [:nativeCurrencyName string?]
     [:nativeCurrencyDecimals int?]]]
   [:map {:closed true}
    [:testnet? boolean?]
    [:chain-id int?]
    [:related-chain-id int?]
    [:layer [:enum 1 2]]
    [:short-name string?]
    [:chain-color string?]
    [:full-name string?]
    [:block-explorer-url string?]
    [:native-currency-symbol string?]
    [:native-currency-name string?]
    [:native-currency-decimals int?]
    [:network-name keyword?]
    [:source :schema.common/image-source]
    [:abbreviated-name string?]
    [:block-explorer-name string?]]])

(defn- sort-networks
  [networks]
  (sort-by (juxt :layer :short-name) networks))

(defn rpc->networks
  [networks-data]
  (let [networks
        (reduce
         (fn [result {:keys [Prod Test]}]
           (cond-> result
             Prod (update :prod (fnil conj []) (make-network Prod))
             Test (update :test (fnil conj []) (make-network Test))))
         {:prod [] :test []}
         networks-data)]
    {:prod (->> networks
                :prod
                sort-networks)
     :test (->> networks
                :test
                sort-networks)}))

(defn networks-by-id
  [networks]
  (->> networks
       vals
       (apply concat)
       (into {} (map (juxt :chain-id identity)))))
