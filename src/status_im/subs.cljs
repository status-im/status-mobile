(ns status-im.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub subscribe]]
            status-im.chat.subs
            status-im.chats-list.subs
            status-im.group-settings.subs
            status-im.discover.subs
            status-im.contacts.subs
            status-im.new-group.subs
            status-im.participants.subs
            status-im.transactions.subs))

(register-sub :get
  (fn [db [_ k]]
    (reaction (k @db))))

(register-sub :get-current-account
  (fn [db [_ _]]
    (reaction (let [current-account-id (:current-account-id @db)]
                (get-in @db [:accounts current-account-id])))))

(register-sub :get-in
  (fn [db [_ path]]
    (reaction (get-in @db path))))

(register-sub :signed-up?
  (fn []
    (let [account-id (subscribe [:get :current-account-id])
          accounts (subscribe [:get :accounts])]
      (reaction (when (and @accounts @account-id)
                  (get-in @accounts [@account-id :signed-up?]))))))

(register-sub :tabs-hidden?
  (fn []
    (let [search-mode?        (subscribe [:get-in [:toolbar-search :show]])
          chats-edit-mode?    (subscribe [:get-in [:chat-list-ui-props :edit?]])
          contacts-edit-mode? (subscribe [:get-in [:contacts-ui-props :edit?]])
          view-id             (subscribe [:get :view-id])]
      (reaction (or @search-mode?
                    (and (= @view-id :chat-list) @chats-edit-mode?)
                    (and (= @view-id :contact-list) @contacts-edit-mode?))))))
