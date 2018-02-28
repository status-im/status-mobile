(ns status-im.ui.screens.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            status-im.chat.subs
            status-im.commands.subs
            status-im.ui.screens.accounts.subs
            status-im.ui.screens.home.subs
            status-im.ui.screens.group.chat-settings.subs
            status-im.ui.screens.discover.subs
            status-im.ui.screens.contacts.subs
            status-im.ui.screens.group.subs
            status-im.ui.screens.wallet.subs
            status-im.ui.screens.wallet.request.subs
            status-im.ui.screens.wallet.send.subs
            status-im.ui.screens.wallet.settings.subs
            status-im.ui.screens.wallet.transactions.subs
            status-im.ui.screens.network-settings.subs
            status-im.ui.screens.browser.subs
            status-im.bots.subs
            status-im.ui.screens.add-new.new-chat.subs))

(reg-sub :get
  (fn [db [_ k]]
    (get db k)))

(reg-sub :get-in
  (fn [db [_ path]]
    (get-in db path)))

(reg-sub :signed-up?
  :<- [:get-current-account]
  (fn [current-account]
    (:signed-up? current-account)))

(reg-sub :network
  (fn [db]
    (:network db)))

(reg-sub :sync-state
  (fn [db]
    (:sync-state db)))

(reg-sub :syncing?
  :<- [:sync-state]
  (fn [sync-state]
    (#{:pending :in-progress} sync-state)))

(reg-sub :get-screen-params
  (fn [db [_ view-id]]
    (get-in db [:navigation/screen-params (or view-id (:view-id db))])))

(reg-sub :can-navigate-back?
  (fn [db]
    (> (count (:navigation-stack db)) 1)))