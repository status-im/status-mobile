(ns status-im.ui.screens.home.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :home-items
 :<- [:get-active-chats]
 :<- [:browser/browsers]
 (fn [[chats browsers]]
   (sort-by #(-> % second :timestamp) > (merge chats browsers))))

(re-frame/reg-sub
 :chain-sync-state
 (fn [{:node/keys [chain-sync-state]} _] chain-sync-state))

(re-frame/reg-sub
 :current-network-uses-rpc?
 (fn [db _]
   (let [network (get-in db [:account/account :networks (:network db)])]
     (get-in network [:config :UpstreamConfig :Enabled]))))

(re-frame/reg-sub
 :latest-block-number
 (fn [{:node/keys [latest-block-number]} _]
   (if latest-block-number latest-block-number 0)))
