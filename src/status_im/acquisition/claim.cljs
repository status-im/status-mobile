(ns status-im.acquisition.claim
  (:require [status-im.utils.fx :as fx]
            [status-im.ethereum.transactions.core :as transaction]
            [status-im.acquisition.notifications :as notifications]
            [status-im.acquisition.persistance :as persistence]
            [status-im.ethereum.json-rpc :as json-rpc]
            [re-frame.core :as re-frame]))

(fx/defn success-tx-received
  {:events [::success-tx-received]}
  [cofx]
  (fx/merge cofx
            (notifications/claimed)
            {::persistence/set-referrer-state :claimed}))

(fx/defn add-tx-watcher
  {:events [::add-tx-watcher]}
  [cofx tx]
  (fx/merge cofx
            {::persistence/set-watch-tx tx}
            (transaction/watch-transaction tx
                                           {:trigger-fn (constantly true)
                                            :on-trigger
                                            (fn []
                                              {:dispatch [::success-tx-received]})})))

(fx/defn check-transaction-receipt
  {:events [::check-transaction-receipt]}
  [cofx tx]
  (when tx
    {::json-rpc/call [{:method     "eth_getTransactionReceipt"
                       :params     [tx]
                       :on-success (fn [receipt]
                                     (if receipt
                                       (re-frame/dispatch [::success-tx-received])
                                       (re-frame/dispatch [::add-tx-watcher tx])))}]}))

(fx/defn success-starter-pack-claim
  {:events [::success-starter-pack-claim]}
  [cofx {:keys [tx]}]
  (fx/merge cofx
            {::persistence/set-referrer-state (if tx :accepted :claimed)}
            (when tx
              (add-tx-watcher tx))))
