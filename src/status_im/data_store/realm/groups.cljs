(ns status-im.data-store.realm.groups
  (:require [status-im.data-store.realm.core :as realm]
    [status-im.utils.random :refer [timestamp]])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  []
  (-> @realm/account-realm
      (realm/get-all :group)))

(defn get-all-as-list
  []
  (realm/realm-collection->list (get-all)))

(defn save
  [group update?]
  (realm/save @realm/account-realm :group group update?))

(defn exists?
  [group-id]
  (realm/exists? @realm/account-realm :group {:group-id group-id}))