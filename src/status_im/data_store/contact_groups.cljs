(ns status-im.data-store.contact-groups
  (:require [status-im.data-store.realm.contact-groups :as data-store])
  (:refer-clojure :exclude [exists?]))

(defn- normalize-contacts
  [item]
  (update item :contacts vals))

(defn get-all
  []
  (map normalize-contacts (data-store/get-all-as-list)))

(defn save
  [{:keys [group-id] :as group}]
  (data-store/save group (data-store/exists? group-id)))

(defn save-all
  [groups]
  (mapv save groups))

(defn delete
  [group-id]
  (data-store/delete group-id))
