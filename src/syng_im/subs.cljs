(ns syng-im.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.models.chats :refer [chats-list chat-by-id]]
            [syng-im.models.contacts :refer [contacts-list
                                             contacts-list-exclude
                                             contacts-list-include]]
            syng-im.chat.subs
            syng-im.navigation.subs
            syng-im.components.discovery.subs))

;; -- Chats list --------------------------------------------------------------

(register-sub :get-chats
  (fn [db _]
    (reaction (:chats @db))))

;; -- User data --------------------------------------------------------------

;; (register-sub
;;   :get-user-phone-number
;;   (fn [db _]
;;     (reaction
;;       (get @db :user-phone-number))))

(register-sub
  :signed-up
  (fn [db _]
    (reaction (:signed-up @db))))

(register-sub
  :get-contacts
  (fn [db _]
    (reaction (:contacts @db))))

(register-sub :all-contacts
  (fn [_ _]
    (reaction (contacts-list))))

(register-sub :all-new-contacts
  (fn [db _]
    (let [current-chat-id (reaction (:current-chat-id @db))
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
    (let [current-chat-id (reaction (:current-chat-id @db))
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
