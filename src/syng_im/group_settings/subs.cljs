(ns syng-im.group-settings.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.db :as db]
            [syng-im.models.chats :refer [chats-list chat-by-id]]
            [syng-im.models.contacts :refer [get-contacts
                                             contacts-list-exclude
                                             contacts-list-include
                                             contact-by-identity]]
            [syng-im.handlers.content-suggestions :refer [get-content-suggestions]]))

(register-sub :get-current-chat-name
  (fn [db _]
    (reaction (get-in @db (db/chat-name-path (:current-chat-id @db))))))

(register-sub :get-current-chat-color
  (fn [db _]
    (reaction (get-in @db (db/chat-color-path (:current-chat-id @db))))))

(register-sub :selected-group-chat-member
  (fn [db [_]]
    (reaction
     (let [identity (get-in @db db/group-settings-selected-member-path)]
       (contact-by-identity identity)))))

(register-sub :group-settings-show-color-picker
  (fn [db [_]]
    (reaction (get-in @db db/group-settings-show-color-picker))))
