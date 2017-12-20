(ns status-im.ui.screens.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            status-im.chat.subs
            status-im.commands.subs
            status-im.ui.screens.accounts.subs
            status-im.ui.screens.chats-list.subs
            status-im.ui.screens.group.chat-settings.subs
            status-im.ui.screens.discover.subs
            status-im.ui.screens.contacts.subs
            status-im.ui.screens.group.subs
            status-im.ui.screens.profile.subs
            status-im.ui.screens.wallet.subs
            status-im.ui.screens.wallet.request.subs
            status-im.ui.screens.wallet.send.subs
            status-im.ui.screens.wallet.settings.subs
            status-im.ui.screens.wallet.transactions.subs
            status-im.ui.screens.wallet.wallet-list.subs
            status-im.ui.screens.wallet.assets.subs
            status-im.ui.screens.network-settings.subs
            status-im.bots.subs))

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

(reg-sub :tabs-hidden?
  :<- [:get-in [:toolbar-search :show]]
  :<- [:get-in [:chat-list-ui-props :edit?]]
  :<- [:get-in [:contacts/ui-props :edit?]]
  :<- [:get :view-id]
  (fn [[search-mode? chats-edit-mode? contacts-edit-mode? view-id]]
    (or search-mode?
        (and (= view-id :chat-list) chats-edit-mode?)
        (and (= view-id :contact-list) contacts-edit-mode?))))

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
