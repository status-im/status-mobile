(ns status-im.data-store.contacts
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.data-store.realm.core :as core]))

(defn- deserialize-contact [contact]
  (-> contact
      (update :tags #(into #{} %))
      (update :tribute-to-talk core/deserialize)
      (update :system-tags
              #(reduce (fn [acc s]
                         (conj acc (keyword (subs s 1))))
                       #{}
                       %))))

(defn- serialize-contact [contact]
  (-> contact
      (update :device-info #(or (vals %) []))
      (update :system-tags #(mapv str %))
      (update :tribute-to-talk core/serialize)))

(re-frame/reg-cofx
 :data-store/get-all-contacts
 (fn [coeffects _]
   (assoc coeffects :all-contacts (map deserialize-contact
                                       (-> @core/account-realm
                                           (core/get-all :contact)
                                           (core/all-clj :contact))))))

(defn save-contact-tx
  "Returns tx function for saving contact"
  [{:keys [public-key] :as contact}]
  (fn [realm]
    (core/create realm
                 :contact
                 (serialize-contact contact)
                 true)))

(defn save-contacts-tx
  "Returns tx function for saving contacts"
  [contacts]
  (fn [realm]
    (doseq [contact contacts]
      ((save-contact-tx contact) realm))))

(defn- get-contact-by-id [public-key realm]
  (.objectForPrimaryKey realm "contact" public-key))

(defn- get-messages-by-messages-ids
  [message-ids]
  (when (not-empty message-ids)
    (-> @core/account-realm
        (.objects "message")
        (.filtered (str "(" (core/in-query "message-id" message-ids) ")")))))

(defn- get-chat
  [public-key]
  (core/single
   (core/get-by-field @core/account-realm
                      :chat
                      :chat-id
                      public-key)))

(defn block-user-tx
  "Returns tx function for deleting user messages"
  [{:keys [public-key] :as contact} messages-ids]
  (fn [realm]
    (core/create realm :contact (serialize-contact contact) true)
    (when-let [user-messages
               (get-messages-by-messages-ids messages-ids)]
      (core/delete realm user-messages))
    (when-let [chat
               (get-chat public-key)]
      (core/delete realm chat))))

(defn delete-contact-tx
  "Returns tx function for deleting contact"
  [public-key]
  (fn [realm]
    (core/delete realm (get-contact-by-id public-key realm))))
