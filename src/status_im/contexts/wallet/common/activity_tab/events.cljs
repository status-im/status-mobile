(ns status-im.contexts.wallet.common.activity-tab.events
  (:require [camel-snake-kebab.extras :as cske]
            [utils.ethereum.chain :as chain]
            [utils.re-frame :as rf]
            [utils.transforms :as transforms]))

(defonce ^:private request-id-atom (atom 0))

(defn- get-unique-request-id
  []
  (swap! request-id-atom inc)
  @request-id-atom)

(rf/reg-event-fx
 :wallet/fetch-activities-for-current-account
 (fn [{:keys [db]}]
   (let [address        (-> db :wallet :current-viewing-account-address)
         chain-ids      (chain/chain-ids db)
         request-id     (get-unique-request-id)
         filters        {:period                {:startTimestamp 0
                                                 :endTimestamp   0}
                         :types                 []
                         :statuses              []
                         :counterpartyAddresses []
                         :assets                []
                         :collectibles          []
                         :filterOutAssets       false
                         :filterOutCollectibles false}
         offset         0
         limit          50
         request-params [request-id [address] chain-ids filters offset limit]]
     {:db (assoc-in db [:wallet :ui :activity-tab :request request-id] address)
      :fx [[:json-rpc/call
            [{;; This method is deprecated and will be replaced by
              ;; "wallet_startActivityFilterSession"
              ;; https://github.com/status-im/status-mobile/issues/19864
              :method   "wallet_filterActivityAsync"
              :params   request-params
              :on-error [:wallet/log-rpc-error
                         {:event  :wallet/fetch-activities-for-current-account
                          :params request-params}]}]]]})))

(def ^:private activity-transaction-id (comp hash :transaction))

(rf/reg-event-fx
 :wallet/activity-filtering-for-current-account-done
 (fn [{:keys [db]} [{:keys [message requestId]}]]
   (let [address            (get-in db [:wallet :ui :activity-tab :request requestId])
         activities         (->> message
                                 (transforms/json->clj)
                                 (:activities)
                                 (cske/transform-keys transforms/->kebab-case-keyword))
         activities-indexed (zipmap (map activity-transaction-id activities)
                                    activities)]
     {:db (assoc-in db [:wallet :activities address] activities-indexed)})))

(def ^:private nested-merge (partial merge-with merge))

(rf/reg-event-fx
 :wallet/activities-filtering-entries-updated
 (fn [{:keys [db]} [{:keys [message requestId]}]]
   (let [address            (get-in db [:wallet :ui :activity-tab :request requestId])
         activities         (->> message
                                 (transforms/json->clj)
                                 (cske/transform-keys transforms/->kebab-case-keyword))
         activities-indexed (zipmap (map activity-transaction-id activities)
                                    activities)]
     {:db (-> db
              (update-in [:wallet :ui :activity-tab :request] dissoc requestId)
              (update-in [:wallet :activities address] nested-merge activities-indexed))})))
