(ns status-im.data-store.realm.networks
  (:require [status-im.data-store.realm.core :as realm])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  []
  (-> @realm/account-realm
      (realm/get-all :network)))

(defn get-all-as-list
  []
  (realm/realm-collection->list (get-all)))

(defn save
  [network update?]
  (realm/save @realm/account-realm :network network update?))

(defn save-property
  [id property-name value]
  (realm/write @realm/account-realm
               (fn []
                 (-> @realm/account-realm
                     (realm/get-one-by-field :network :id id)
                     (aset (name property-name) value)))))

(defn exists?
  [id]
  (realm/exists? @realm/account-realm :network {:id id}))

(defn delete
  [id]
  (when-let [network (realm/get-one-by-field @realm/account-realm :network :id id)]
    (realm/delete @realm/account-realm network)))