(ns status-im.contexts.wallet.common.activity-tab.events
  (:require [camel-snake-kebab.extras :as cske]
            [utils.collection :as collection]
            [utils.ethereum.chain :as chain]
            [utils.re-frame :as rf]
            [utils.transforms :as transforms]))

(def ^:const limit-per-request 20)

(defn- nested-merge
  [& maps]
  (apply merge-with merge maps))

(defn- create-default-filters
  []
  {:period                {:startTimestamp 0
                           :endTimestamp   0}
   :types                 []
   :statuses              []
   :counterpartyAddresses []
   :assets                []
   :collectibles          []
   :filterOutAssets       false
   :filterOutCollectibles false})

(rf/reg-event-fx
 :wallet/store-session-id-for-activity-filter-session
 (fn [{:keys [db]} [request-id]]
   {:db (assoc-in db [:wallet :ui :activity-tab :request :request-id] request-id)}))

(rf/reg-event-fx
 :wallet/fetch-activities-for-current-account
 (fn [{:keys [db]}]
   (let [address   (get-in db [:wallet :current-viewing-account-address])
         chain-ids (chain/chain-ids db)
         params    [[address] chain-ids (create-default-filters) limit-per-request]]
     {:db (-> db
              (update-in [:wallet :activities] dissoc address)
              (update-in [:wallet :ui :activity-tab :request] dissoc :request-id)
              (update-in [:wallet :ui :activity-tab :request]
                         assoc
                         :address          address
                         :loading?         true
                         :initial-request? true))
      :fx [[:json-rpc/call
            [{:method     "wallet_startActivityFilterSessionV2"
              :params     params
              :on-success [:wallet/store-session-id-for-activity-filter-session]
              :on-error   [:wallet/log-rpc-error
                           {:event  :wallet/fetch-activities-for-current-account
                            :params params}]}]]]})))

(rf/reg-event-fx
 :wallet/stop-activity-filter-session
 (fn [{:keys [db]}]
   (when-let [session-id (get-in db [:wallet :ui :activity-tab :request :request-id])]
     {:db (update-in db [:wallet :ui :activity-tab] dissoc :request)
      :fx [[:json-rpc/call
            [{:method   "wallet_stopActivityFilterSession"
              :params   [session-id]
              :on-error [:wallet/log-rpc-error
                         {:event  :wallet/stop-activity-filter-session
                          :params [session-id]}]}]]]})))

(rf/reg-event-fx
 :wallet/get-more-for-activities-filter-session
 (fn [{:keys [db]}]
   (let [session-id (get-in db [:wallet :ui :activity-tab :request :request-id])
         has-more?  (get-in db [:wallet :ui :activity-tab :request :has-more?])
         params     [session-id limit-per-request]]
     (when (and session-id has-more?)
       {:fx [[:json-rpc/call
              [{:method   "wallet_getMoreForActivityFilterSession"
                :params   params
                :on-error [:wallet/log-rpc-error
                           {:event  :wallet/get-more-for-activities-filter-session
                            :params params}]}]]]}))))

(rf/reg-event-fx
 :wallet/reset-activities-filter-session
 (fn [{:keys [db]}]
   (when-let [session-id (get-in db [:wallet :ui :activity-tab :request :request-id])]
     {:db (assoc-in db [:wallet :ui :activity-tab :request :initial-request?] true)
      :fx [[:json-rpc/call
            [{:method   "wallet_resetActivityFilterSession"
              :params   [session-id limit-per-request]
              :on-error [:wallet/log-rpc-error
                         {:event  :wallet/reset-activities-filter-session
                          :params [session-id limit-per-request]}]}]]]})))

(rf/reg-event-fx
 :wallet/activity-filtering-for-current-account-done
 (fn [{:keys [db]} [{:keys [message]}]]
   (let [{:keys [address initial-request?]}  (get-in db [:wallet :ui :activity-tab :request])
         {:keys [activities offset hasMore]} (transforms/json->clj message)
         new-activities                      (->> activities
                                                  (cske/transform-keys transforms/->kebab-case-keyword)
                                                  (collection/index-by :key))
         existing-activities                 (get-in db [:wallet :activities address])
         updated-activities                  (if initial-request?
                                               new-activities
                                               (nested-merge existing-activities new-activities))]
     {:db (-> db
              (assoc-in [:wallet :activities address] updated-activities)
              (assoc-in [:wallet :ui :activity-tab :request :offset] offset)
              (assoc-in [:wallet :ui :activity-tab :request :has-more?] hasMore)
              (assoc-in [:wallet :ui :activity-tab :request :loading?] false)
              (assoc-in [:wallet :ui :activity-tab :request :initial-request?] false))})))

(rf/reg-event-fx
 :wallet/activities-filtering-entries-updated
 (fn [{:keys [db]} [{:keys [message requestId]}]]
   (when (= requestId (get-in db [:wallet :ui :activity-tab :request :request-id]))
     (let [address            (get-in db [:wallet :ui :activity-tab :request :address])
           updated-activities (->> message
                                   transforms/json->clj
                                   (cske/transform-keys transforms/->kebab-case-keyword)
                                   (collection/index-by :key))]
       {:db (update-in db [:wallet :activities address] nested-merge updated-activities)}))))

(rf/reg-event-fx
 :wallet/activities-session-updated
 (fn [_ [{:keys [message]}]]
   (let [{:keys [hasNewOnTop]} (transforms/json->clj message)]
     (when hasNewOnTop
       {:fx [[:dispatch [:wallet/reset-activities-filter-session]]]}))))
