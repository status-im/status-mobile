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

(register-sub :all-contacts
  (fn [db _]
    (let [contacts (reaction (:contacts @db))]
      (reaction (sort-by :name @contacts)))))

(defn contacts-by-current-chat [fn db]
  (let [current-chat-id (:current-chat-id @db)
        chat            (reaction (get-in @db [:chats current-chat-id]))
        contacts        (reaction (:contacts @db))]
    (reaction
      (when @chat
        (let [current-participants (->> @chat
                                        :contacts
                                        (map :identity)
                                        set)]
          (fn #(current-participants (:whisper-identity %))
              (vals @contacts)))))))

(register-sub :all-new-contacts
  (fn [db _]
    (contacts-by-current-chat remove db)))

(register-sub :current-chat-contacts
  (fn [db _]
    (contacts-by-current-chat filter db)))

(register-sub :db
  (fn [db _] (reaction @db)))
