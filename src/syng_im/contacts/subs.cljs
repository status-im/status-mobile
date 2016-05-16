(ns syng-im.contacts.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

(register-sub :get-contacts
  (fn [db _]
    (let [contacts (reaction (:contacts @db))]
      (reaction (vals @contacts)))))

(register-sub :all-contacts
  (fn [db _]
    (let [contacts (reaction (:contacts @db))]
      (reaction (sort-by :name (vals @contacts))))))

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

(register-sub :contact
  (fn [db _]
    (let [identity (:contact-identity @db)]
      (reaction (get-in db [:contacts identity])))))

(register-sub :all-new-contacts
  (fn [db _]
    (contacts-by-current-chat remove db)))

(register-sub :current-chat-contacts
  (fn [db _]
    (contacts-by-current-chat filter db)))
