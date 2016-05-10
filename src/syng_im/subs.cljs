(ns syng-im.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.db :as db]
            [syng-im.models.chat :refer [current-chat-id
                                         chat-updated?]]
            [syng-im.models.chats :refer [chats-list
                                          chats-updated?
                                          chat-by-id]]
            [syng-im.models.messages :refer [get-messages]]
            [syng-im.models.contacts :refer [contacts-list
                                             contacts-list-exclude
                                             contacts-list-include
                                             contact-identity
                                             contact-by-identity]]
            [syng-im.models.commands :refer [get-commands
                                             get-chat-command
                                             get-chat-command-content
                                             get-chat-command-request
                                             parse-command-request]]
            [syng-im.handlers.suggestions :refer [get-suggestions
                                                  typing-command?]]
            [syng-im.handlers.content-suggestions :refer [get-content-suggestions]]))

;; -- Chat --------------------------------------------------------------

(register-sub :get-chat-messages
  (fn [db _]
    (let [chat-id (current-chat-id @db)]
      (reaction (get-in @db [:chats chat-id :messages])))))

(register-sub :get-current-chat-id
  (fn [db _]
    (reaction (current-chat-id @db))))

(register-sub :get-suggestions
  (fn [db _]
    (let [input-text (->> (current-chat-id @db)
                          db/chat-input-text-path
                          (get-in @db)
                          (reaction))]
      (reaction (get-suggestions @db @input-text)))))

(register-sub :typing-command?
  (fn [db _]
    (reaction (typing-command? @db))))

(register-sub :get-content-suggestions
  (fn [db _]
    (let [command (reaction (get-chat-command @db))
          text    (reaction (get-chat-command-content @db))]
      (reaction (get-content-suggestions @db @command @text)))))

(register-sub :get-commands
  (fn [db _]
    (reaction (get-commands @db))))

(register-sub :get-chat-input-text
  (fn [db _]
    (reaction (get-in @db (db/chat-input-text-path (current-chat-id @db))))))

(register-sub :get-chat-staged-commands
  (fn [db _]
    (reaction (get-in @db (db/chat-staged-commands-path (current-chat-id @db))))))

(register-sub :get-chat-command
  (fn [db _]
    (reaction (get-chat-command @db))))

(register-sub :get-chat-command-content
  (fn [db _]
    (reaction (get-chat-command-content @db))))

(register-sub :chat-command-request
  (fn [db _]
    (reaction (get-chat-command-request @db))))

;; -- Chats list --------------------------------------------------------------

(register-sub :get-chats
  (fn [db _]
    (let [chats-updated (reaction (chats-updated? @db))]
      (reaction
        (let [_ @chats-updated]
          (chats-list))))))

(register-sub :get-current-chat
  (fn [db _]
    (let [current-chat-id (current-chat-id @db)]
      (reaction (get-in @db [:chats current-chat-id])))))

;; -- User data --------------------------------------------------------------

;; (register-sub
;;   :get-user-phone-number
;;   (fn [db _]
;;     (reaction
;;       (get @db :user-phone-number))))

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
  :signed-up
  (fn [db _]
    (reaction
      (get @db :signed-up))))

(register-sub
  :show-actions
  (fn [db _]
    (reaction (get-in @db db/show-actions-path))))

(register-sub
  :get-contacts
  (fn [db _]
    (reaction
      (get @db :contacts))))

(register-sub :all-contacts
  (fn [db _]
    (reaction
      (contacts-list))))

(register-sub :contact
   (fn [db _]
     (let [identity (reaction (get-in @db db/contact-identity-path))]
       (reaction (contact-by-identity @identity)))))

(register-sub :all-new-contacts
  (fn [db _]
    (let [current-chat-id (reaction (current-chat-id @db))
          chat            (reaction (when-let [chat-id @current-chat-id]
                                      (chat-by-id chat-id)))]
      (reaction
        (when @chat
          (let [current-participants (->> @chat
                                          :contacts
                                          (map :identity))]
            (contacts-list-exclude current-participants)))))))

(register-sub :current-chat-contacts
  (fn [db _]
    (let [current-chat-id (reaction (current-chat-id @db))
          chat            (reaction (when-let [chat-id @current-chat-id]
                                      (chat-by-id chat-id)))]
      (reaction
        (when @chat
          (let [current-participants (->> @chat
                                          :contacts
                                          (map :identity))]
            (contacts-list-include current-participants)))))))

(register-sub :view-id
  (fn [db _]
    (reaction (@db :view-id))))

(register-sub :chat
  (fn [db [_ k]]
    (-> @db
        (get-in [:chats (current-chat-id @db) k])
        (reaction))))

(register-sub :navigation-stack
  (fn [db _]
    (reaction (:navigation-stack @db))))

(register-sub :db
  (fn [db _] (reaction @db)))

(register-sub :chat-properties
  (fn [db [_ properties]]
    (->> properties
         (map (fn [k]
                [k (-> @db
                       (get-in [:chats (:current-chat-id @db) k])
                       (reaction))]))
         (into {}))))
