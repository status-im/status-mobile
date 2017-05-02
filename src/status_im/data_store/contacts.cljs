(ns status-im.data-store.contacts
  (:require [status-im.data-store.realm.contacts :as data-store])
  (:refer-clojure :exclude [exists?]))

(defn- command->map-item
  [[_ {:keys [name] :as command}]]
  [(keyword name) command])

(defn get-all
  []
  (map
    (fn [{:keys [commands responses] :as contact}]
      (assoc contact
        :commands (into {} (map command->map-item commands))
        :responses (into {} (map command->map-item responses))))
    (data-store/get-all-as-list)))

(defn get-by-id
  [whisper-identity]
  (data-store/get-by-id-cljs whisper-identity))

(defn save
  [{:keys [whisper-identity pending?] :as contact}]
  (let [{pending-db? :pending?
         :as         contact-db} (data-store/get-by-id whisper-identity)
        contact (assoc contact :pending?
                               (boolean (if contact-db
                                          (if (nil? pending?) pending-db? pending?)
                                          pending?)))]
    (data-store/save contact (if contact-db true false))))

(defn save-all
  [contacts]
  (mapv save contacts))

(defn delete [contact]
  (data-store/delete contact))

(defn exists?
  [whisper-identity]
  (data-store/exists? whisper-identity))
