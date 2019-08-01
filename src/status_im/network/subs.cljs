(ns status-im.network.subs
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]))

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
    (let [custom? (not (contains? default-networks (:id network)))]
      (assoc network :custom? custom?))))

(re-frame/reg-sub
 :get-networks
 :<- [:multiaccount]
 :<- [:networks/networks]
 (fn [[{:keys [:networks/networks]} default-networks]]
   (let [networks (map (label-networks default-networks) (sort-by :name (vals networks)))
         types    [:mainnet :testnet :custom]]
     (zipmap
      types
      (map #(filter (filter-networks %) networks) types)))))

(re-frame/reg-sub
 :get-manage-network
 :<- [:networks/manage]
 (fn [manage]
   manage))

(re-frame/reg-sub
 :manage-network-valid?
 :<- [:get-manage-network]
 (fn [manage]
   (not-any? :error (vals manage))))
