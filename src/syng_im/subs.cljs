(ns syng-im.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.models.chat :refer [current-chat-id chat-updated?]]
            [syng-im.models.chats :refer [chats-list chats-updated? chat-by-id]]
            [syng-im.models.contacts :refer [contacts-list
                                             contacts-list-exclude
                                             contacts-list-include]]
            syng-im.chat.subs))

;; -- Chats list --------------------------------------------------------------

(register-sub :get-chats
  (fn [db _]
    (let [chats-updated (reaction (chats-updated? @db))]
      (reaction
        (let [_ @chats-updated]
          (chats-list))))))

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

(register-sub :db
  (fn [db _] (reaction @db)))

