(ns status-im.data-store.contacts
  (:require [status-im.data-store.realm.contacts :as data-store])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  []
  (data-store/get-all-as-list))

(defn get-by-id
  [whisper-identity]
  (data-store/get-by-id-cljs whisper-identity))

(defn save
  [{:keys [whisper-identity pending?] :as contact}]
  (let [{pending-db? :pending?
         :as         contact-db} (get-by-id whisper-identity)
        contact' (-> contact
                     (assoc :pending? (boolean (if contact-db
                                                 (if (nil? pending?) pending-db? pending?)
                                                 pending?)))
                     (dissoc :command :response :subscriptions :jail-loaded-events))]
    (data-store/save contact' (boolean contact-db))))

(defn save-all
  [contacts]
  (mapv save contacts))

(defn delete [contact]
  (data-store/delete contact))

(defn exists?
  [whisper-identity]
  (data-store/exists? whisper-identity))
