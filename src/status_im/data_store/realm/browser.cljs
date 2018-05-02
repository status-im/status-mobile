(ns status-im.data-store.realm.browser
  (:require [status-im.data-store.realm.core :as realm])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  []
  (-> @realm/account-realm
      (realm/get-all :browser)
      (realm/sorted :timestamp :desc)
      (realm/all-clj :browser)))

(defn save
  [browser update?]
  (realm/save @realm/account-realm :browser browser update?))

(defn delete
  [browser-id]
  (when-let [browser (realm/single (realm/get-by-field @realm/account-realm :browser :browser-id browser-id))]
    (realm/write @realm/account-realm #(realm/delete @realm/account-realm browser))))

(defn exists?
  [browser-id]
  (realm/exists? @realm/account-realm :browser :browser-id browser-id))

(defn get-by-id
  [browser-id]
  (-> @realm/account-realm
      (realm/get-by-field :browser :browser-id browser-id)
      (realm/single-clj :browser)))
