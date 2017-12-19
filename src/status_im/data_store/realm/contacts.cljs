(ns status-im.data-store.realm.contacts
  (:require [status-im.data-store.realm.core :as realm])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  []
  (-> @realm/account-realm
      (realm/get-all :contact)
      (realm/sorted :name :asc)))

(defn get-all-as-list
  []
  (realm/js-object->clj (get-all)))

(defn get-by-id
  [whisper-identity]
  (realm/get-one-by-field @realm/account-realm :contact :whisper-identity whisper-identity))

(defn get-by-id-cljs
  [whisper-identity]
  (realm/get-one-by-field-clj @realm/account-realm :contact :whisper-identity whisper-identity))

(defn save
  [contact update?]
  (realm/save @realm/account-realm :contact contact update?))

(defn delete
  [{:keys [whisper-identity]}]
  (realm/delete @realm/account-realm (get-by-id whisper-identity)))

(defn exists?
  [whisper-identity]
  (realm/exists? @realm/account-realm :contact {:whisper-identity whisper-identity}))
