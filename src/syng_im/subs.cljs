(ns syng-im.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.models.chats :refer [chats-list chat-by-id]]
            [syng-im.models.contacts :refer [get-contacts
                                             contacts-list-exclude
                                             contacts-list-include]]
            syng-im.chat.subs
            syng-im.navigation.subs
            syng-im.discovery.subs
            syng-im.contacts.subs))

;; -- Chats list --------------------------------------------------------------

(register-sub :get
  (fn [db [_ k]]
    (reaction (k @db))))

;; -- User data --------------------------------------------------------------

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
    (reaction (get-contacts))))

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
