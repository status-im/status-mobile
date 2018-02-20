(ns status-im.data-store.realm.contact-groups
  (:require [goog.object :as object]
            [status-im.data-store.realm.core :as realm])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  []
  (-> @realm/account-realm
      (realm/get-all :contact-group)))

(defn get-all-as-list
  []
  (realm/js-object->clj (get-all)))

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
      (object/get "contacts")))

(defn- save-contacts
  [identities contacts]
  (doseq [contact-identity identities]
    (when-not (.find contacts (fn [object _ _]
                                (= contact-identity (object/get object "identity"))))
      (.push contacts (clj->js {:identity contact-identity})))))

(defn add-contacts
  [group-id identities]
  (let [contacts (get-contacts group-id)]
    (realm/write @realm/account-realm
                 #(save-contacts identities contacts))))
