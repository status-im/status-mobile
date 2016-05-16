(ns syng-im.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.models.chats :refer [chats-list chat-by-id]]
            [syng-im.models.contacts :refer [get-contacts
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

;; -- User data --------------------------------------------------------------
(register-sub
  :signed-up
  (fn [db _]
    (reaction (:signed-up @db))))

(register-sub :db
  (fn [db _] (reaction @db)))
