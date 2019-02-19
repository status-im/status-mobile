(ns status-im.data-store.contacts
  (:require [goog.object :as object]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]
            [clojure.set :as clojure.set]))

(defn- normalize-contact [contact]
  (-> contact
      (update :tags #(into #{} %))))

(defn- serialize-contact [contact]
  (update contact :device-info #(or (vals %) [])))

(re-frame/reg-cofx
 :data-store/get-all-contacts
 (fn [coeffects _]
   (assoc coeffects :all-contacts (map normalize-contact
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
  (core/single (core/get-by-field realm :contact :public-key public-key)))

(defn- get-messages-by-messages-ids
  [message-ids]
  (when (not-empty message-ids)
    (-> @core/account-realm
        (.objects "message")
        (.filtered (str "(" (core/in-query "message-id" message-ids) ")")))))

(defn- get-statuses-by-messages-ids
  [message-ids]
  (when-not (empty message-ids)
    (-> @core/account-realm
        (.objects "user-status")
        (.filtered (str "(" (core/in-query "message-id" message-ids) ")")))))

(defn- get-user-statuses
  [public-key]
  (core/get-by-field @core/account-realm
                     :user-status
                     :public-key
                     public-key))

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
    (when-let [user-messages-statuses
               (get-statuses-by-messages-ids messages-ids)]
      (core/delete realm user-messages-statuses))
    (when-let [user-statuses
               (get-user-statuses public-key)]
      (core/delete realm user-statuses))
    (when-let [chat
               (get-chat public-key)]
      (core/delete realm chat))))

(defn delete-contact-tx
  "Returns tx function for deleting contact"
  [public-key]
  (fn [realm]
    (core/delete realm (get-contact-by-id public-key realm))))

(defn add-contact-tag-tx
  "Returns tx function for adding chat contacts"
  [public-key tag]
  (fn [realm]
    (let [contact       (get-contact-by-id public-key realm)
          existing-tags (object/get contact "tags")]
      (aset contact "tags"
            (clj->js (into #{} (concat tag
                                       (core/list->clj existing-tags))))))))

(defn remove-contact-tag-tx
  "Returns tx function for removing chat contacts"
  [public-key tag]
  (fn [realm]
    (let [contact       (get-contact-by-id public-key realm)
          existing-tags (object/get contact "tags")]
      (aset contact "tags"
            (clj->js (remove (into #{} tag)
                             (core/list->clj existing-tags)))))))
