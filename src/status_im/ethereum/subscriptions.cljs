(ns status-im.ethereum.subscriptions
  (:require [status-im.ethereum.eip55 :as eip55]
            [status-im.wallet.db :as wallet]
            [status-im.wallet.core :as wallet.core]
            [status-im.ethereum.transactions.core :as transactions]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(fx/defn new-transfers
  [cofx block-number accounts]
  (log/debug "[wallet-subs] new-transfers"
             "accounts" accounts
             "block" block-number)
  (transactions/check-watched-transactions cofx))

(fx/defn recent-history-fetching-started
  [{:keys [db]} accounts]
  (log/debug "[wallet-subs] recent-history-fetching-started"
             "accounts" accounts)
  (let [event (get db :wallet/on-recent-history-fetching)]
    (cond-> {:db (-> db
                     (transactions/update-fetching-status accounts :recent? true)
                     (assoc :wallet/recent-history-fetching-started? true)
                     (dissoc :wallet/on-recent-history-fetching))}
      event
      (assoc :dispatch event))))

(fx/defn recent-history-fetching-ended
  [{:keys [db]} {:keys [accounts blockNumber]}]
  (log/debug "[wallet-subs] recent-history-fetching-ended"
             "accounts" accounts
             "block" blockNumber)
  {:db (-> db
           (assoc :ethereum/current-block blockNumber)
           (update-in [:wallet :accounts]
                      wallet/remove-transactions-since-block blockNumber)
           (transactions/update-fetching-status accounts :recent? false)
           (dissoc :wallet/waiting-for-recent-history?
                   :wallet/refreshing-history?
                   :wallet/fetching-error
                   :wallet/recent-history-fetching-started?))
   :transactions/get-transfers
   {:chain-tokens (:wallet/all-tokens db)
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

(fx/defn fetching-error
  [{:keys [db] :as cofx} {:keys [message]}]
  (fx/merge
   cofx
   {:db               (assoc db :wallet/fetching-error message)}
   (wallet.core/after-checking-history)))

(fx/defn non-archival-node-detected
  [{:keys [db]} _]
  {:db (assoc db :wallet/non-archival-node true)})

(fx/defn new-wallet-event
  [cofx {:keys [type blockNumber accounts] :as event}]
  (log/info "[wallet-subs] new-wallet-event"
            "event-type" type
            "blockNumber" blockNumber
            "accounts" accounts)
  (case type
    "new-transfers" (new-transfers cofx blockNumber accounts)
    "recent-history-fetching" (recent-history-fetching-started cofx accounts)
    "recent-history-ready" (recent-history-fetching-ended cofx event)
    "fetching-history-error" (fetching-error cofx event)
    "non-archival-node-detected" (non-archival-node-detected cofx event)
    (log/warn ::unknown-wallet-event :type type :event event)))
