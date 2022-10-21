(ns status-im.subs.networks
  (:require [status-im.ethereum.core :as ethereum]
            [re-frame.core :as re-frame]
            [status-im.utils.config :as config]))

(defn- filter-networks [network-type]
  (fn [network]
    (let [chain-id (ethereum/network->chain-id network)
          testnet? (ethereum/testnet? chain-id)
          custom?  (:custom? network)]
      (case network-type
        :custom custom?
        :mainnet (and (not custom?) (not testnet?))
        :testnet (and (not custom?) testnet?)))))

(defn- label-networks [default-networks]
  (fn [network]
    (let [custom? (not (default-networks (:id network)))]
      (assoc network :custom? custom?))))

(re-frame/reg-sub
 :get-networks
 :<- [:networks/networks]
 (fn [networks]
   (let [networks (map (label-networks (into #{} (map :id config/default-networks))) (sort-by :name (vals networks)))
         types    [:mainnet :testnet :custom]]
     (zipmap
      types
      (map #(filter (filter-networks %) networks) types)))))

(re-frame/reg-sub
 :manage-network-valid?
 :<- [:networks/manage]
 (fn [manage]
   (not-any? :error (vals manage))))
