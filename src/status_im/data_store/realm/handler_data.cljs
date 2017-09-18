(ns status-im.data-store.realm.handler-data
  (:require [status-im.data-store.realm.core :as realm]))

(defn get-all []
  (realm/get-all @realm/account-realm :handler-data))

(defn get-all-as-list []
  (realm/realm-collection->list (get-all)))

(defn get-by-message-id
  [message-id]
  (realm/get-one-by-field-clj @realm/account-realm :handler-data :message-id message-id))

(defn save
  [handler-data]
  (realm/save @realm/account-realm :handler-data handler-data true))
