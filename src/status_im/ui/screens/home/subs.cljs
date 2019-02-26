(ns status-im.ui.screens.home.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.platform :as platform]))

(re-frame/reg-sub
 :home-items
 :<- [:chats/active-chats]
 :<- [:browser/browsers]
 :<- [:search/filter]
 :<- [:search/filtered-chats]
 :<- [:search/filtered-browsers]
 (fn [[chats browsers search-filter filtered-chats filtered-browsers]]
   (if (or (nil? search-filter)
           (and platform/desktop? (empty? search-filter)))
     {:all-home-items
      (sort-by #(-> % second :timestamp) >
               (merge chats browsers))}
     {:search-filter search-filter
      :chats filtered-chats
      :browsers filtered-browsers})))

(re-frame/reg-sub
 :chain-sync-state
 (fn [{:node/keys [chain-sync-state]} _] chain-sync-state))

(re-frame/reg-sub
 :current-network-initialized?
 (fn [db _]
   (let [network (get-in db [:account/account :networks (:network db)])]
     (boolean network))))

(re-frame/reg-sub
 :current-network-uses-rpc?
 (fn [db _]
   (let [network (get-in db [:account/account :networks (:network db)])]
     (get-in network [:config :UpstreamConfig :Enabled]))))

(re-frame/reg-sub
 :latest-block-number
 (fn [{:node/keys [latest-block-number]} _]
   (if latest-block-number latest-block-number 0)))
