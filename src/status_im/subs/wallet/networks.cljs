(ns status-im.subs.wallet.networks
  (:require [re-frame.core :as re-frame]
            [status-im.contexts.wallet.common.utils.networks :as network-utils]
            [utils.money :as money]
            [utils.number :as number]))

(re-frame/reg-sub
 :wallet/networks
 :<- [:wallet]
 :-> :networks)

(re-frame/reg-sub
 :wallet/networks-by-id
 :<- [:wallet]
 :-> :networks-by-id)

(re-frame/reg-sub
 :wallet/network-details
 :<- [:wallet/networks]
 :<- [:profile/test-networks-enabled?]
 (fn [[networks test-networks-enabled?]]
   (get networks (if test-networks-enabled? :test :prod))))

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
 :<- [:wallet/networks-by-id]
 (fn [networks [_ chain-id]]
   (get networks chain-id)))

(re-frame/reg-sub
 :wallet/network-name-from-chain-id
 :<- [:wallet/networks-by-id]
 (fn [networks [_ chain-id]]
   (-> networks (get chain-id) :network-name)))

(re-frame/reg-sub
 :wallet/selected-network-details
 :<- [:wallet/network-details]
 :<- [:wallet/selected-networks]
 (fn [[network-details selected-networks]]
   (filter
    #(contains? selected-networks (:network-name %))
    network-details)))

(re-frame/reg-sub
 :wallet/network-values
 :<- [:wallet/networks-by-id]
 :<- [:wallet/wallet-send]
 (fn [[networks {:keys [from-values-by-chain to-values-by-chain token-display-name token] :as send-data}]
      [_ to-values?]]
   (let [network-values (if to-values? to-values-by-chain from-values-by-chain)
         token-symbol   (or token-display-name
                            (-> send-data :token :symbol))
         token-decimals (:decimals token)]
     (reduce-kv
      (fn [acc chain-id amount]
        (let [network-name (-> networks (get chain-id) :network-name)
              amount-fixed (number/to-fixed (money/->bignumber amount) token-decimals)]
          (merge acc (network-utils/network-summary network-name token-symbol amount-fixed))))
      {}
      network-values))))

(re-frame/reg-sub
 :wallet/send-selected-network
 :<- [:wallet/networks-by-id]
 :<- [:wallet/wallet-send]
 (fn [[networks {:keys [to-values-by-chain]}]]
   (->> to-values-by-chain
        keys
        first
        (get networks))))
