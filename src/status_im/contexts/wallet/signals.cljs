(ns status-im.contexts.wallet.signals
  (:require
    [oops.core :as oops]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/reg-event-fx
 :wallet/pending-transaction-status-changed-received
 (fn [{:keys [db]} [{:keys [message]}]]
   (let [details (js->clj (js/JSON.parse message) :keywordize-keys true)
         tx-hash (:hash details)]
     {:db (update-in db [:wallet :transactions tx-hash] assoc :status :confirmed :blocks 1)})))

(rf/reg-event-fx
 :wallet/signal-received
 (fn [_ [event-js]]
   (let [event-type  (oops/oget event-js "type")
         blockNumber (oops/oget event-js "blockNumber")
         accounts    (oops/oget event-js "accounts")]
     (log/debug "[wallet-subs] New wallet event"
                {:type         event-type
                 :block-number blockNumber
                 :accounts     accounts})
     (case event-type
       "pending-transaction-status-changed"       {:fx
                                                   [[:dispatch
                                                     [:wallet/pending-transaction-status-changed-received
                                                      (js->clj event-js
                                                               :keywordize-keys
                                                               true)]]]}
       "wallet-owned-collectibles-filtering-done" {:fx [[:dispatch
                                                         [:wallet/owned-collectibles-filtering-done
                                                          (js->clj event-js
                                                                   :keywordize-keys
                                                                   true)]]]}
       "wallet-get-collectibles-details-done"     {:fx [[:dispatch
                                                         [:wallet/get-collectible-details-done
                                                          (js->clj event-js
                                                                   :keywordize-keys
                                                                   true)]]]}
       "wallet-tick-reload"                       {:fx [[:dispatch [:wallet/reload]]]}
       (log/debug ::unknown-wallet-event
                  :type  type
                  :event (js->clj event-js
                                  :keywordize-keys
                                  true))))))
