(ns status-im.contexts.wallet.signals
  (:require 
   [taoensso.timbre :as log]
   [utils.re-frame :as rf]))

(rf/reg-event-fx
 :wallet/pending-transaction-status-changed-received
 (fn [{:keys [db]} [{:keys [message]}]]
   (let [details (js->clj (js/JSON.parse message) :keywordize-keys true)
         tx-hash (:hash details)]
     {:db (update-in db [:wallet :transactions tx-hash] assoc :status :confirmed :blocks 1)})))

(rf/reg-event-fx
 :wallet/signal-fired
 (fn [_ {:keys [type blockNumber accounts] :as event}]
   (log/debug "[wallet-subs] new-wallet-event"
              "event-type"  type
              "blockNumber" blockNumber
              "accounts"    accounts)
   (case type
     "pending-transaction-status-changed"       {:fx
                                                 [[:dispatch
                                                   [:wallet/pending-transaction-status-changed-received
                                                    event]]]}
     "wallet-owned-collectibles-filtering-done" {:fx [[:dispatch
                                                       [:wallet/owned-collectibles-filtering-done
                                                        event]]]}
     "wallet-get-collectibles-details-done"     {:fx [[:dispatch
                                                       [:wallet/get-collectible-details-done
                                                        event]]]}
      "wallet-tick-reload"                       {:fx [[:dispatch [:wallet/reload]]]}
     (log/debug ::unknown-wallet-event :type type :event event))))
