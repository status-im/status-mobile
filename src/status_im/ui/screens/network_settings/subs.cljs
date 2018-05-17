(ns status-im.ui.screens.network-settings.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [status-im.utils.ethereum.core :as ethereum]
            status-im.ui.screens.network-settings.edit-network.subs))

(reg-sub
 :get-network-id
 :<- [:network]
 (fn [network]
   (ethereum/network->chain-id network)))

(defn- filter-networks [network-type]
  (fn [network]
    (let [chain-id (ethereum/network->chain-id network)
          testnet? (ethereum/testnet? chain-id)
          custom?  (:custom? network)]
      (case network-type
        :custom  custom?
        :mainnet (and (not custom?) (not testnet?))
        :testnet (and (not custom?) testnet?)))))

(defn- label-networks [default-networks]
  (fn [network]
    (let [custom? (not (contains? default-networks (:id network)))]
      (assoc network :custom? custom?))))

(reg-sub
 :get-networks
 :<- [:get :account/account]
 :<- [:get :networks/networks]
 (fn [[{:keys [networks] :as account} default-networks]]
   (let [networks (map (label-networks default-networks) (sort-by :name (vals networks)))
         types    [:mainnet :testnet :custom]]
     (zipmap
      types
      (map #(filter (filter-networks %) networks) types)))))
