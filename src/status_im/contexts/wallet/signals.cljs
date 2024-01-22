(ns status-im.contexts.wallet.signals
  (:require [utils.re-frame :as rf]))

(rf/reg-event-fx
 :wallet/pending-transaction-status-changed-received
 (fn [{:keys [db]} [{:keys [message]}]]
   (let [details (js->clj (js/JSON.parse message) :keywordize-keys true)
         tx-hash (:hash details)]
     {:db (update-in db [:wallet :transactions tx-hash] assoc :status :confirmed :blocks 1)})))

