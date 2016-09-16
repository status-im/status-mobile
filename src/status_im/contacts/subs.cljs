(ns status-im.contacts.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub subscribe]]
            [status-im.utils.identicon :refer [identicon]]))

(register-sub :get-contacts
  (fn [db _]
    (let [contacts (reaction (:contacts @db))]
      (reaction @contacts))))

(defn sort-contacts [contacts]
  (sort-by :name #(compare (clojure.string/lower-case %1)
                           (clojure.string/lower-case %2)) (vals contacts)))

(register-sub :all-added-contacts
  (fn [db _]
    (let [contacts (reaction (:contacts @db))]
      (->> (remove #(:pending %) @contacts)
           (sort-contacts)
           (reaction)))))

(register-sub :get-added-contacts-with-limit
  (fn [_ [_ limit]]
    (let [contacts (subscribe [:all-added-contacts])]
      (reaction (take limit @contacts)))))

(register-sub :added-contacts-count
  (fn [_ _]
    (let [contacts (subscribe [:all-added-contacts])]
      (reaction (count @contacts)))))

(defn get-contact-letter [contact]
  (when-let [letter (first (:name contact))]
    (clojure.string/upper-case letter)))

(register-sub :contacts-with-letters
  (fn [db _]
    (let [contacts (reaction (:contacts @db))]
      (reaction
        (let [ordered (sort-contacts @contacts)]
          (reduce (fn [prev cur]
                    (let [prev-letter (get-contact-letter (last prev))
                          cur-letter  (get-contact-letter cur)]
                      (conj prev
                            (if (not= prev-letter cur-letter)
                              (assoc cur :letter cur-letter)
                              cur))))
                  [] ordered))))))

(defn contacts-by-chat [fn db chat-id]
  (let [chat     (reaction (get-in @db [:chats chat-id]))
        contacts (reaction (:contacts @db))]
    (reaction
      (when @chat
        (let [current-participants (->> @chat
                                        :contacts
                                        (map :identity)
                                        set)]
          (fn #(current-participants (:whisper-identity %))
              (vals @contacts)))))))

(defn contacts-by-current-chat [fn db]
  (let [current-chat-id (:current-chat-id @db)]
    (contacts-by-chat fn db current-chat-id)))

(register-sub :contact
  (fn [db _]
    (let [identity (:contact-identity @db)]
      (reaction (get-in @db [:contacts identity])))))

(register-sub :contact-by-identity
  (fn [db [_ identity]]
    (reaction (get-in @db [:contacts identity]))))

(register-sub :all-new-contacts
  (fn [db _]
    (contacts-by-current-chat remove db)))

(register-sub :current-chat-contacts
  (fn [db _]
    (contacts-by-current-chat filter db)))

(register-sub :chat-photo
  (fn [db [_ chat-id]]
    (let [chat-id  (or chat-id (:current-chat-id @db))
          chat     (reaction (get-in @db [:chats chat-id]))
          contacts (contacts-by-chat filter db chat-id)]
      (reaction
        (when @chat
          (if (:group-chat @chat)
            ;; TODO return group chat icon
            nil
            (if (pos? (count @contacts))
              (:photo-path (first @contacts))
              (identicon chat-id))))))))
