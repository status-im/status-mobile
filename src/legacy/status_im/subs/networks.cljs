(ns legacy.status-im.subs.networks
  (:require
    [re-frame.core :as re-frame]
    [status-im2.config :as config]
    [utils.ethereum.chain :as chain]))

(defn- filter-networks
  [network-type]
  (fn [network]
    (let [chain-id (chain/network->chain-id network)
          testnet? (chain/testnet? chain-id)
          custom?  (:custom? network)]
      (case network-type
        :custom  custom?
        :mainnet (and (not custom?) (not testnet?))
        :testnet (and (not custom?) testnet?)))))

(defn- label-networks
  [default-networks]
  (fn [network]
    (let [custom? (not (default-networks (:id network)))]
      (assoc network :custom? custom?))))

(re-frame/reg-sub
 :get-networks
 :<- [:networks/networks]
 (fn [networks]
   (let [networks (map (label-networks (into #{} (map :id config/default-networks)))
                       (sort-by :name (vals networks)))
         types    [:mainnet :testnet :custom]]
     (zipmap
      types
      (map #(filter (filter-networks %) networks) types)))))

(re-frame/reg-sub
 :manage-network-valid?
 :<- [:networks/manage]
 (fn [manage]
   (not-any? :error (vals manage))))
