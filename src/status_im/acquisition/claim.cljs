(ns status-im.acquisition.claim
  (:require [status-im.i18n :as i18n]
            [status-im.utils.fx :as fx]
            [status-im.ethereum.transactions.core :as transaction]
            [status-im.notifications.core :as notifications]
            [status-im.acquisition.persistance :as persistence]))

(fx/defn success-tx-received
  {:events [::success-tx-received]}
  [_]
  {::persistence/set-referrer-state   :claimed
   ::notifications/local-notification {:title   (i18n/label :t/starter-pack-received)
                                       :message (i18n/label :t/starter-pack-received-description)}})

(fx/defn add-tx-watcher
  [cofx tx]
  (transaction/watch-transaction cofx
                                 tx
                                 {:trigger-fn (constantly true)
                                  :on-trigger
                                  (fn []
                                    {:dispatch [::success-tx-received]})}))

(fx/defn success-starter-pack-claim
  {:events [::success-starter-pack-claim]}
  [cofx {:keys [tx]}]
  (fx/merge cofx
            {::persistence/set-watch-tx       tx
             ::persistence/set-referrer-state :accepted}
            (add-tx-watcher tx)
            (notifications/request-permission)))
