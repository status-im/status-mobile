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
 :wallet/networks-by-mode
 :<- [:wallet/networks]
 :<- [:profile/test-networks-enabled?]
 (fn [[networks test-networks-enabled?]]
   (get networks (if test-networks-enabled? :test :prod))))

(re-frame/reg-sub
 :wallet/network-details
 :<- [:wallet/networks-by-mode]
 (fn [networks]
   (network-utils/sorted-networks-with-details networks)))

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
   (network-utils/format-address address network-preferences)))

(re-frame/reg-sub
 :wallet/network-values
 :<- [:wallet/wallet-send]
 (fn [{:keys [from-values-by-chain to-values-by-chain token-display-name token] :as send-data}
      [_ to-values?]]
   (let [network-values (if to-values? to-values-by-chain from-values-by-chain)
         token-symbol   (or token-display-name
                            (-> send-data :token :symbol))
         token-decimals (:decimals token)]
     (reduce-kv
      (fn [acc chain-id amount]
        (let [network-name (network-utils/id->network chain-id)
              amount-fixed (number/to-fixed (money/->bignumber amount) token-decimals)]
          (assoc acc
                 (if (= network-name :mainnet) :ethereum network-name)
                 {:amount amount-fixed :token-symbol token-symbol})))
      {}
      network-values))))
