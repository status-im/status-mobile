(ns syng-im.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.models.chats :refer [chats-list chat-by-id]]
            [syng-im.models.contacts :refer [get-contacts
                                         current-chat
                                             contacts-list-exclude
                                             contacts-list-include
                                             contact-by-identity]]
            syng-im.chat.subs
            syng-im.navigation.subs
            syng-im.discovery.subs
            syng-im.contacts.subs))

;; -- Chats list --------------------------------------------------------------

(register-sub :get
  (fn [db [_ k]]
    (reaction (k @db))))
(register-sub :get-current-chat-name
  (fn [db _]
    (let [current-chat-id (current-chat-id @db)]
      (reaction (get-in @db (db/chat-name-path current-chat-id))))))
(register-sub :get-current-chat-color
  (fn [db _]
    (let [current-chat-id (current-chat-id @db)]
      (reaction (get-in @db (db/chat-color-path current-chat-id))))))

;; -- User data --------------------------------------------------------------
(register-sub
  :signed-up
  (fn [db _]
    (reaction (:signed-up @db))))

(register-sub :selected-group-chat-member
  (fn [db [_]]
    (reaction
     (let [identity (get-in @db db/group-settings-selected-member-path)]
       (contact-by-identity identity)))))

(register-sub :group-settings-show-color-picker
  (fn [db [_]]
    (reaction (get-in @db db/group-settings-show-color-picker))))

(register-sub :db
  (fn [db _] (reaction @db)))
