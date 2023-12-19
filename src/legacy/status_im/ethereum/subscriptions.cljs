(ns legacy.status-im.ethereum.subscriptions
  (:require
    [legacy.status-im.ethereum.transactions.core :as transactions]
    [legacy.status-im.wallet.core :as wallet.core]
    [legacy.status-im.wallet.db :as wallet]
    [taoensso.timbre :as log]
    [utils.ethereum.eip.eip55 :as eip55]
    [utils.re-frame :as rf]))

(rf/defn new-transfers
  [cofx block-number accounts]
  (log/debug "[wallet-subs] new-transfers"
             "accounts" accounts
             "block"    block-number)
  (transactions/check-watched-transactions cofx))

(rf/defn recent-history-fetching-started
  [{:keys [db]} accounts]
  (log/debug "[wallet-subs] recent-history-fetching-started"
             "accounts"
             accounts)
  (let [event (get db :wallet-legacy/on-recent-history-fetching)]
    (cond-> {:db (-> db
                     (transactions/update-fetching-status accounts :recent? true)
                     (assoc :wallet-legacy/recent-history-fetching-started? true)
                     (dissoc :wallet-legacy/on-recent-history-fetching))}
      event
      (assoc :dispatch event))))

(rf/defn recent-history-fetching-ended
  [{:keys [db]} {:keys [accounts blockNumber]}]
  (log/debug "[wallet-subs] recent-history-fetching-ended"
             "accounts" accounts
             "block"    blockNumber)
  {:db (-> db
           (assoc :ethereum/current-block blockNumber)
           (update-in [:wallet-legacy :accounts]
                      wallet/remove-transactions-since-block
                      blockNumber)
           (transactions/update-fetching-status accounts :recent? false)
           (dissoc :wallet-legacy/waiting-for-recent-history?
                   :wallet-legacy/refreshing-history?
                   :wallet-legacy/fetching-error
                   :wallet-legacy/recent-history-fetching-started?))
   :transactions/get-transfers
   {:chain-tokens (:wallet-legacy/all-tokens db)
    :addresses    (reduce
                   (fn [v address]
                     (let [normalized-address
                           (eip55/address->checksum address)]
                       (if (contains? v normalized-address)
                         v
                         (conj v address))))
                   []
                   accounts)
    :before-block blockNumber
    :limit        20}})

(rf/defn fetching-error
  [{:keys [db] :as cofx} {:keys [message]}]
  (rf/merge
   cofx
   {:db (assoc db :wallet-legacy/fetching-error message)}
   (wallet.core/after-checking-history)))

(rf/defn non-archival-node-detected
  [{:keys [db]} _]
  {:db (assoc db :wallet-legacy/non-archival-node true)})

(rf/defn new-wallet-event
  [cofx {:keys [type blockNumber accounts] :as event}]
  (log/debug "[wallet-subs] new-wallet-event"
             "event-type"  type
             "blockNumber" blockNumber
             "accounts"    accounts)
  (case type
    "new-transfers"                            (new-transfers cofx blockNumber accounts)
    "recent-history-fetching"                  (recent-history-fetching-started cofx accounts)
    "recent-history-ready"                     (recent-history-fetching-ended cofx event)
    "fetching-history-error"                   (fetching-error cofx event)
    "non-archival-node-detected"               (non-archival-node-detected cofx event)
    "wallet-owned-collectibles-filtering-done" {:fx [[:dispatch
                                                      [:wallet/owned-collectibles-filtering-done
                                                       event]]]}
    "wallet-get-collectibles-details-done"     {:fx [[:dispatch
                                                      [:wallet/get-collectible-details-done
                                                       event]]]}
    (log/debug ::unknown-wallet-event :type type :event event)))
