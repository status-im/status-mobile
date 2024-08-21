(ns status-im.contexts.wallet.signals
  (:require
    [oops.core :as oops]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(rf/reg-event-fx
 :wallet/pending-transaction-status-changed-received
 (fn [{:keys [db]} [{:keys [message]}]]
   (let [details (transforms/json->clj message)
         tx-hash (:hash details)]
     {:db (update-in db [:wallet :transactions tx-hash] assoc :status :confirmed :blocks 1)})))

(rf/reg-event-fx
 :wallet/signal-received
 (fn [_ [event-js]]
   (let [event-type  (oops/oget event-js "type")
         blockNumber (oops/oget event-js "blockNumber")
         accounts    (oops/oget event-js "accounts")]
     (log/debug "[wallet] Wallet signal received"
                {:type         event-type
                 :block-number blockNumber
                 :accounts     accounts})
     (case event-type
       "pending-transaction-status-changed"
       {:fx
        [[:dispatch
          [:wallet/pending-transaction-status-changed-received
           (transforms/js->clj event-js)]]]}

       "wallet-owned-collectibles-filtering-done"
       {:fx [[:dispatch
              [:wallet/owned-collectibles-filtering-done
               (transforms/js->clj event-js)]]]}

       "wallet-get-collectibles-details-done"
       {:fx [[:dispatch
              [:wallet/get-collectible-details-done
               (transforms/js->clj event-js)]]]}

       "wallet-tick-reload"
       {:fx [[:dispatch [:wallet/reload]]]}

       "wallet-blockchain-status-changed"
       {:fx [[:dispatch-later
              ;; Don't dispatch immediately because the signal may arrive as
              ;; soon as the device goes offline. We need to give some time for
              ;; RN to dispatch the network status update, otherwise when going
              ;; offline the user will immediately see a toast saying "provider
              ;; X is down".
              [{:ms       500
                :dispatch [:wallet/blockchain-status-changed
                           (transforms/js->clj event-js)]}]]]}

       "wallet-activity-filtering-done"
       {:fx
        [[:dispatch
          [:wallet/activity-filtering-for-current-account-done
           (transforms/js->clj event-js)]]]}

       "wallet-activity-filtering-entries-updated"
       {:fx [[:dispatch
              [:wallet/activities-filtering-entries-updated
               (transforms/js->clj event-js)]]]}

       (log/debug ::unknown-wallet-event :type event-type)))))
