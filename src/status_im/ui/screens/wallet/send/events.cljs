(ns status-im.ui.screens.wallet.send.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.wallet.db :as wallet.db]))

(re-frame/reg-event-db
  :choose-recipient
  (fn [db [_ recipient]]
    (assoc-in db [:wallet :send :recipient] recipient)))
