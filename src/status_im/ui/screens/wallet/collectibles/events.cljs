(ns status-im.ui.screens.wallet.collectibles.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]))

(handlers/register-handler-fx
 :load-collectible-success
 [re-frame/trim-v]
 (fn [{db :db} [symbol collectibles]]
   {:db (update-in db [:collectibles symbol] merge collectibles)}))

(handlers/register-handler-fx
 :load-collectibles-failure
 [re-frame/trim-v]
 (fn [{db :db} [{:keys [message]}]]
   {:db (assoc db :collectibles-failure message)}))

(handlers/register-handler-fx
 :load-collectible-failure
 [re-frame/trim-v]
 (fn [{db :db} [_]]
   {:db db}))
