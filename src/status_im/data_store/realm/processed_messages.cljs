(ns status-im.data-store.realm.processed-messages
  (:require [status-im.data-store.realm.core :as realm])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  []
  (-> @realm/account-realm
      (realm/get-all :processed-message)
      (realm/sorted :ttl :asc)))

(defn get-filtered
  [condition]
  (realm/filtered (get-all) condition))

(defn get-filtered-as-list
  [condition]
  (realm/js-object->clj (get-filtered condition)))

(defn save
  [processed-message]
  (realm/save @realm/account-realm :processed-message processed-message))

(defn delete
  [condition]
  (realm/delete @realm/account-realm (get-filtered condition)))
