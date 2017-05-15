(ns status-im.data-store.networks
  (:require [status-im.data-store.realm.networks :as data-store])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  []
  (data-store/get-all-as-list))

(defn save
  [{:keys [id] :as network}]
  (data-store/save network (data-store/exists? id)))

(defn save-all
  [networks]
  (mapv save networks))

(defn save-property
  [id property-name value]
  (data-store/save-property id property-name value))

(defn delete
  [id]
  (data-store/delete id))
