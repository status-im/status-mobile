(ns status-im.data-store.realm.contacts
  (:require [status-im.data-store.realm.core :as realm])
  (:refer-clojure :exclude [exists?]))

(defn get-all-as-list
  []
  (realm/all-clj (realm/get-all @realm/account-realm :contact) :contact))

(defn get-by-id
  [whisper-identity]
  (realm/single (realm/get-by-field @realm/account-realm :contact :whisper-identity whisper-identity)))

(defn get-by-id-cljs
  [whisper-identity]
  (-> @realm/account-realm
      (realm/get-by-field :contact :whisper-identity whisper-identity)
      (realm/single-clj :contact)))

(defn save
  [contact update?]
  (realm/save @realm/account-realm :contact contact update?))

(defn delete
  [{:keys [whisper-identity]}]
  (realm/write @realm/account-realm
               #(realm/delete @realm/account-realm (get-by-id whisper-identity))))

(defn exists?
  [whisper-identity]
  (realm/exists? @realm/account-realm :contact :whisper-identity whisper-identity))
