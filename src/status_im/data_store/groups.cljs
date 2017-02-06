(ns status-im.data-store.groups
  (:require [status-im.data-store.realm.groups :as data-store]
    [re-frame.core :refer [dispatch]])
  (:refer-clojure :exclude [exists?]))

(defn- normalize-contacts
  [groups]
  (map #(update % :contacts vals) groups))

(defn get-all
  []
  (normalize-contacts (data-store/get-all-as-list)))

(defn save
  [{:keys [group-id] :as group}]
  (data-store/save group (data-store/exists? group-id)))

(defn save-all
  [contacts]
  (mapv save contacts))
