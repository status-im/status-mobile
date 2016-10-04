(ns status-im.data-store.contacts
  (:require [status-im.data-store.realm.contacts :as data-store])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  []
  (data-store/get-all-as-list))

(defn get-by-id
  [whisper-identity]
  (data-store/get-by-id whisper-identity))

(defn save
  [{:keys [whisper-identity pending] :as contact}]
  (let [{pending-db :pending
         :as        contact-db} (data-store/get-by-id whisper-identity)
        contact (assoc contact :pending (boolean (if contact-db
                                                   (and pending-db pending)
                                                   pending)))]
    (data-store/save contact (if contact-db true false))))

(defn save-all
  [contacts]
  (mapv save contacts))

(defn exists?
  [whisper-identity]
  (data-store/exists? whisper-identity))