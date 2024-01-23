(ns status-im.subs.wallet.networks
  (:require [quo.foundations.resources :as resources]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]))

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
  {:source       (resources/get-network constants/mainnet-network-name)
   :short-name   constants/mainnet-short-name
   :network-name constants/mainnet-network-name})

(def arbitrum-network-details
  {:source       (resources/get-network constants/arbitrum-network-name)
   :short-name   constants/arbitrum-short-name
   :network-name constants/arbitrum-network-name})

(def optimism-network-details
  {:source       (resources/get-network constants/optimism-network-name)
   :short-name   constants/optimism-short-name
   :network-name constants/optimism-network-name})

(def network-list
  {constants/mainnet-chain-id  mainnet-network-details
   constants/goerli-chain-id   mainnet-network-details
   constants/arbitrum-chain-id arbitrum-network-details
   constants/optimism-chain-id optimism-network-details})

(re-frame/reg-sub
 :wallet/network-details
 :<- [:wallet/networks-by-mode]
 (fn [networks]
   (->> networks
        (keep
         (fn [{:keys [chain-id related-chain-id layer test?]}]
           (let [network-details (get network-list (if test? related-chain-id chain-id))]
             (assoc network-details
                    :chain-id         chain-id
                    :related-chain-id related-chain-id
                    :layer            layer))))
        (sort-by (juxt :layer :short-name)))))

(re-frame/reg-sub
 :wallet/network-details-by-chain-id
 :<- [:wallet/network-details]
 (fn [networks [_ chain-id]]
   (some #(when (= chain-id (:chain-id %)) %) networks)))
