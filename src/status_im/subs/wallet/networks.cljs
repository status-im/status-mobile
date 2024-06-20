(ns status-im.subs.wallet.networks
  (:require [quo.foundations.resources :as resources]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils.networks :as network-utils]))

(def max-network-prefixes 2)

(re-frame/reg-sub
 :wallet/networks
 :<- [:wallet]
 :-> :networks)

(re-frame/reg-sub
 :wallet/networks-by-mode
 :<- [:wallet/networks]
 :<- [:profile/test-networks-enabled?]
 (fn [[networks test-networks-enabled?]]
   (get networks (if test-networks-enabled? :test :prod))))

(def mainnet-network-details
  {:source           (resources/get-network constants/mainnet-network-name)
   :short-name       constants/mainnet-short-name
   :network-name     constants/mainnet-network-name
   :abbreviated-name constants/mainnet-abbreviated-name})

(def arbitrum-network-details
  {:source           (resources/get-network constants/arbitrum-network-name)
   :short-name       constants/arbitrum-short-name
   :network-name     constants/arbitrum-network-name
   :abbreviated-name constants/arbitrum-abbreviated-name})

(def optimism-network-details
  {:source           (resources/get-network constants/optimism-network-name)
   :short-name       constants/optimism-short-name
   :network-name     constants/optimism-network-name
   :abbreviated-name constants/optimism-abbreviated-name})

(defn get-network-details
  [chain-id]
  (condp contains? chain-id
    #{constants/ethereum-mainnet-chain-id constants/ethereum-goerli-chain-id
      constants/ethereum-sepolia-chain-id}
    mainnet-network-details

    #{constants/arbitrum-mainnet-chain-id constants/arbitrum-goerli-chain-id
      constants/arbitrum-sepolia-chain-id}
    arbitrum-network-details

    #{constants/optimism-mainnet-chain-id constants/optimism-goerli-chain-id
      constants/optimism-sepolia-chain-id}
    optimism-network-details

    nil))

(re-frame/reg-sub
 :wallet/network-details
 :<- [:wallet/networks-by-mode]
 (fn [networks]
   (->> networks
        (map
         (fn [{:keys [chain-id related-chain-id layer]}]
           (assoc (get-network-details chain-id)
                  :chain-id         chain-id
                  :related-chain-id related-chain-id
                  :layer            layer)))
        (sort-by (juxt :layer :short-name)))))

(re-frame/reg-sub
 :wallet/network-details-by-network-name
 :<- [:wallet/network-details]
 (fn [network-details]
   (when (seq network-details)
     (->> network-details
          (group-by :network-name)
          (reduce-kv (fn [acc network-key network-group]
                       (assoc acc network-key (first network-group)))
                     {})))))

(re-frame/reg-sub
 :wallet/network-details-by-chain-id
 :<- [:wallet/network-details]
 (fn [networks [_ chain-id]]
   (some #(when (= chain-id (:chain-id %)) %) networks)))

(re-frame/reg-sub
 :wallet/selected-network-details
 :<- [:wallet/network-details]
 :<- [:wallet/selected-networks]
 (fn [[network-details selected-networks]]
   (filter
    #(contains? selected-networks (:network-name %))
    network-details)))

(re-frame/reg-sub
 :wallet/account-address
 (fn [_ [_ address network-preferences]]
   (let [short-names         (map network-utils/network->short-name network-preferences)
         prefix              (when (<= (count short-names) max-network-prefixes)
                               (network-utils/short-names->network-preference-prefix
                                short-names))
         transformed-address (str prefix address)]
     transformed-address)))
