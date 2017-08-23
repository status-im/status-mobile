(ns status-im.ui.screens.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            status-im.chat.subs
            status-im.ui.screens.accounts.subs
            status-im.ui.screens.chats-list.subs
            status-im.ui.screens.group.chat-settings.subs
            status-im.ui.screens.discover.subs
            status-im.ui.screens.contacts.subs
            status-im.ui.screens.group.subs
            status-im.ui.screens.wallet.subs
            status-im.transactions.subs
            status-im.bots.subs))

(reg-sub :get
  (fn [db [_ k]]
    (get db k)))

(reg-sub :get-current-account
  (fn [db]
    (let [current-account-id (:accounts/current-account-id db)]
      (get-in db [:accounts/accounts current-account-id]))))

(reg-sub :get-in
  (fn [db [_ path]]
    (get-in db path)))

(reg-sub :signed-up?
  :<- [:get :accounts/current-account-id]
  :<- [:get-accounts]
  (fn [[account-id accounts]]
    (when (and accounts account-id)
      (get-in accounts [account-id :signed-up?]))))

(reg-sub :tabs-hidden?
  :<- [:get-in [:toolbar-search :show]]
  :<- [:get-in [:chat-list-ui-props :edit?]]
  :<- [:get-in [:contacts/ui-props :edit?]]
  :<- [:get :view-id]
  (fn [[search-mode? chats-edit-mode? contacts-edit-mode? view-id]]
    (or search-mode?
        (and (= view-id :chat-list) chats-edit-mode?)
        (and (= view-id :contact-list) contacts-edit-mode?))))