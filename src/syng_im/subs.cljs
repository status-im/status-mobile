(ns syng-im.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.db :as db]
            [syng-im.subscriptions.discovery :as discovery]
            [syng-im.models.chat :refer [current-chat-id
                                         chat-updated?]]
            [syng-im.models.chats :refer [chats-list
                                          chats-updated?
                                          chat-by-id]]
            [syng-im.models.messages :refer [get-messages]]
            [syng-im.models.contacts :refer [contacts-list
                                             contacts-list-exclude
                                             contacts-list-include]]
            [syng-im.handlers.suggestions :refer [get-suggestions]]))

;; -- Chat --------------------------------------------------------------

(register-sub :get-chat-messages
  (fn [db _]
    (let [chat-id      (-> (current-chat-id @db)
                           (reaction))
          chat-updated (-> (chat-updated? @db @chat-id)
                           (reaction))]
      (reaction
        (let [_ @chat-updated]
          (get-messages @chat-id))))))

(register-sub :get-current-chat-id
  (fn [db _]
    (-> (current-chat-id @db)
        (reaction))))

(register-sub :get-suggestions
  (fn [db _]
    (let [input-text (reaction (get-in @db (db/chat-input-text-path (current-chat-id @db))))]
      (reaction (get-suggestions @input-text)))))

(register-sub :get-chat-input-text
  (fn [db _]
    (reaction (get-in @db (db/chat-input-text-path (current-chat-id @db))))))

(register-sub :get-chat-command
  (fn [db _]
    (reaction (get-in @db (db/chat-command-path (current-chat-id @db))))))

(register-sub :get-chat-command-content
  (fn [db _]
    (reaction (get-in @db (db/chat-command-content-path (current-chat-id @db))))))

;; -- Chats list --------------------------------------------------------------

(register-sub :get-chats
  (fn [db _]
    (let [chats-updated (-> (chats-updated? @db)
                            (reaction))]
      (reaction
        (let [_ @chats-updated]
          (chats-list))))))

(register-sub :get-current-chat
  (fn [db _]
    (let [current-chat-id (-> (current-chat-id @db)
                              (reaction))
          chat-updated    (-> (chat-updated? @db @current-chat-id)
                              (reaction))]
      (reaction
        (let [_ @chat-updated]
          (when-let [chat-id @current-chat-id]
            (chat-by-id chat-id)))))))



;; -- User data --------------------------------------------------------------

(register-sub
  :get-user-phone-number
  (fn [db _]
    (reaction
      (get @db :user-phone-number))))

(register-sub
  :get-user-identity
  (fn [db _]
    (reaction
      (get @db :user-identity))))

(register-sub
  :get-loading
  (fn [db _]
    (reaction
      (get @db :loading))))

(register-sub
  :get-confirmation-code
  (fn [db _]
    (reaction
      (get @db :confirmation-code))))

(register-sub
  :get-contacts
  (fn [db _]
    (reaction
      (get @db :contacts))))

(register-sub :all-contacts
  (fn [db _]
    (reaction
      (contacts-list))))

(register-sub :all-new-contacts
  (fn [db _]
    (let [current-chat-id (-> (current-chat-id @db)
                              (reaction))
          chat            (-> (when-let [chat-id @current-chat-id]
                                (chat-by-id chat-id))
                              (reaction))]
      (reaction
        (when @chat
          (let [current-participants (->> @chat
                                          :contacts
                                          (map :identity))]
            (contacts-list-exclude current-participants)))))))

(register-sub :current-chat-contacts
  (fn [db _]
    (let [current-chat-id (-> (current-chat-id @db)
                              (reaction))
          chat            (-> (when-let [chat-id @current-chat-id]
                                (chat-by-id chat-id))
                              (reaction))]
      (reaction
        (when @chat
          (let [current-participants (->> @chat
                                          :contacts
                                          (map :identity))]
            (contacts-list-include current-participants)))))))
