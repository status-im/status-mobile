(ns status-im.subs.wallet.dapps.proposals
  (:require [re-frame.core :as rf]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as
             data-store]
            [utils.string]))

(rf/reg-sub
 :wallet-connect/current-proposal-request
 :<- [:wallet-connect/current-proposal]
 :-> :request)

(rf/reg-sub
 :wallet-connect/session-proposal-networks
 :<- [:wallet-connect/current-proposal]
 :-> :session-networks)

(rf/reg-sub
 :wallet-connect/session-proposer
 :<- [:wallet-connect/current-proposal-request]
 (fn [proposal]
   (-> proposal :params :proposer)))

(rf/reg-sub
 :wallet-connect/session-proposer-name
 :<- [:wallet-connect/session-proposer]
 (fn [proposer]
   (let [{:keys [name url]} (-> proposer :metadata)]
     (data-store/compute-dapp-name name url))))

(rf/reg-sub
 :wallet-connect/session-proposal-network-details
 :<- [:wallet-connect/session-proposal-networks]
 :<- [:wallet/network-details]
 (fn [[session-networks network-details]]
   (let [supported-networks       (map :chain-id network-details)
         session-networks         (filterv #(contains? (set session-networks) (:chain-id %))
                                           network-details)
         all-networks-in-session? (= (count supported-networks) (count session-networks))]
     {:session-networks         session-networks
      :all-networks-in-session? all-networks-in-session?})))

(rf/reg-sub
 :wallet-connect/current-proposal-address
 (fn [db]
   (get-in db [:wallet-connect/current-proposal :address])))
