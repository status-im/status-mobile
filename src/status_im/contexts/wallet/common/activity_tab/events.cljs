(ns status-im.contexts.wallet.common.activity-tab.events
  (:require [camel-snake-kebab.extras :as cske]
            [utils.ethereum.chain :as chain]
            [utils.re-frame :as rf]
            [utils.transforms :as transforms]))

(rf/reg-event-fx
 :wallet/fetch-activities-for-current-account
 (fn [{:keys [db]}]
   (let [address        (-> db :wallet :current-viewing-account-address)
         chain-ids      (chain/chain-ids db)
         request-id     0
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
         limit          35
         request-params [request-id [address] chain-ids filters offset limit]]
     {:fx [[:json-rpc/call
            [{;; This method is deprecated and will be replaced by
              ;; "wallet_startActivityFilterSession"
              ;; https://github.com/status-im/status-mobile/issues/19864
              :method   "wallet_filterActivityAsync"
              :params   request-params
              :on-error [:wallet/log-rpc-error
                         {:event  :wallet/fetch-activities-for-current-account
                          :params request-params}]}]]]})))

(rf/reg-event-fx
 :wallet/activity-filtering-for-current-account-done
 (fn [{:keys [db]} [{:keys [message]}]]
   (let [address           (-> db :wallet :current-viewing-account-address)
         activities        (->> message
                                (transforms/json->clj)
                                (:activities)
                                (cske/transform-keys transforms/->kebab-case-keyword))
         sorted-activities (sort :timestamp activities)]
     {:db (assoc-in db [:wallet :activities address] sorted-activities)})))
