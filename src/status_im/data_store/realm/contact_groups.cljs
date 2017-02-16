(ns status-im.data-store.realm.contact-groups
  (:require [status-im.data-store.realm.core :as realm]
    [status-im.utils.random :refer [timestamp]])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  []
  (-> @realm/account-realm
      (realm/get-all :contact-group)))

(defn get-all-as-list
  []
  (realm/realm-collection->list (get-all)))

(defn save
  [group update?]
  (realm/save @realm/account-realm :contact-group group update?))

(defn exists?
  [group-id]
  (realm/exists? @realm/account-realm :contact-group {:group-id group-id}))

(defn delete
  [group-id]
  (when-let [group (realm/get-one-by-field @realm/account-realm :contact-group :group-id group-id)]
    (realm/delete @realm/account-realm group)))