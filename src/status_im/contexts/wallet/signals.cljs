(ns status-im.contexts.wallet.signals
  (:require
    [oops.core :as oops]
    [status-im.constants :as constants]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(rf/reg-event-fx
 :wallet/pending-transaction-status-changed-received
 (fn [{:keys [db]} [{:keys [message]}]]
   (let [details                      (transforms/json->clj message)
         tx-hash                      (:hash details)
         tx-status                    (:status details)
         status                       (cond
                                        (= tx-status constants/transaction-status-success)
                                        :confirmed
                                        (= tx-status constants/transaction-status-pending)
                                        :pending
                                        (= tx-status constants/transaction-status-failed)
                                        :failed)
         swap-approval-transaction-id (get-in db [:wallet :ui :swap :approval-transaction-id])
         swap-approval-transaction?   (= swap-approval-transaction-id tx-hash)
         swap-transaction-ids         (get-in db [:wallet :swap-transaction-ids])
         swap-transaction?            (and swap-transaction-ids
                                           (contains? swap-transaction-ids tx-hash))]
     (cond-> {:db (update-in db [:wallet :transactions tx-hash] assoc :status status)}
       swap-approval-transaction?
       (assoc :fx
              [[:dispatch
                [:wallet.swap/approve-transaction-update
                 {:status status}]]])
       swap-transaction?
       (assoc :fx
              [[:dispatch
                [:wallet.swap/swap-transaction-update
                 {:tx-hash tx-hash
                  :status  status}]]])))))

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

       "wallet-collectibles-ownership-update-finished"
       {:fx [[:dispatch
              [:wallet/collectible-ownership-update-finished
               (transforms/js->clj event-js)]]]}

       "wallet-collectibles-ownership-update-finished-with-error"
       {:fx [[:dispatch
              [:wallet/collectible-ownership-update-finished-with-error
               (transforms/js->clj event-js)]]]}

       "wallet-collectibles-data-updated"
       {:fx [[:dispatch
              [:wallet/collectibles-data-updated
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

       "wallet-blockchain-health-changed"
       {:fx [[:dispatch [:wallet/blockchain-health-changed (transforms/js->clj event-js)]]]}

       "wallet-activity-filtering-done"
       {:fx
        [[:dispatch
          [:wallet/activity-filtering-for-current-account-done
           (transforms/js->clj event-js)]]]}

       "wallet-activity-filtering-entries-updated"
       {:fx [[:dispatch
              [:wallet/activities-filtering-entries-updated
               (transforms/js->clj event-js)]]]}

       "wallet-activity-session-updated"
       {:fx [[:dispatch
              [:wallet/activities-session-updated
               (transforms/js->clj event-js)]]]}

       (log/debug ::unknown-wallet-event :type event-type)))))
