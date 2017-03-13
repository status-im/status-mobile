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

(defn save-property
  [group-id property-name value]
  (realm/write @realm/account-realm
               (fn []
                 (-> @realm/account-realm
                     (realm/get-one-by-field :contact-group :group-id group-id)
                     (aset (name property-name) value)))))

(defn exists?
  [group-id]
  (realm/exists? @realm/account-realm :contact-group {:group-id group-id}))

(defn delete
  [group-id]
  (when-let [group (realm/get-one-by-field @realm/account-realm :contact-group :group-id group-id)]
    (realm/delete @realm/account-realm group)))

(defn get-contacts
  [group-id]
  (-> @realm/account-realm
      (realm/get-one-by-field :contact-group :group-id group-id)
      (aget "contacts")))

(defn- save-contacts
  [identities contacts]
  (doseq [contact-identity identities]
    (if-let [contact (.find contacts (fn [object _ _]
                                       (= contact-identity (aget object "identity"))))]
      (.push contacts (clj->js {:identity contact-identity})))))

(defn add-contacts
  [group-id identities]
  (let [contacts (get-contacts group-id)]
    (realm/write @realm/account-realm
                 #(save-contacts identities contacts))))