(ns status-im.data-store.contacts
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]))

(re-frame/reg-cofx
  :data-store/get-all-contacts
  (fn [coeffects _]
    (assoc coeffects :all-contacts (-> @core/account-realm
                                       (core/get-all :contact)
                                       (core/all-clj :contact)))))

(defn save-contact-tx
  "Returns tx function for saving contact"
  [{:keys [whisper-identity] :as contact}]
  (fn [realm]
    (core/create realm
                 :contact
                 (dissoc contact :command :response :subscriptions :jail-loaded-events)
                 (core/exists? realm :contact :whisper-identity whisper-identity))))

(defn save-contacts-tx
  "Returns tx function for saving contacts"
  [contacts]
  (fn [realm]
    (doseq [contact contacts]
      ((save-contact-tx contact) realm))))

(defn delete-contact-tx
  "Returns tx function for deleting contact"
  [whisper-identity]
  (fn [realm]
    (core/delete realm (core/single (core/get-by-field realm :contact :whisper-identity whisper-identity)))))
