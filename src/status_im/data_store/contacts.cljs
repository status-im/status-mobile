(ns status-im.data-store.contacts
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]))

(defn- deserialize-contact [contact])

(defn- serialize-contact [contact])

(re-frame/reg-cofx
 :data-store/get-all-contacts
 (fn [coeffects _]
   coeffects))

(defn save-contact-tx
  "Returns tx function for saving contact"
  [{:keys [public-key] :as contact}]
  (fn [realm]))

(defn save-contacts-tx
  "Returns tx function for saving contacts"
  [contacts]
  (fn [realm]
    (doseq [contact contacts]
      ((save-contact-tx contact) realm))))

(defn- get-contact-by-id [public-key realm])

(defn- get-messages-by-messages-ids
  [message-ids])

(defn- get-chat
  [public-key])

(defn block-user-tx
  "Returns tx function for deleting user messages"
  [{:keys [public-key] :as contact} messages-ids]
  (fn [realm]))

(defn delete-contact-tx
  "Returns tx function for deleting contact"
  [public-key]
  (fn [realm]))
