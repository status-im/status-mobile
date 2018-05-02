(ns status-im.data-store.realm.contact-groups
  (:require [goog.object :as object]
            [status-im.data-store.realm.core :as realm])
  (:refer-clojure :exclude [exists?]))

(defn get-all-as-list
  []
  (realm/all-clj (realm/get-all @realm/account-realm :contact-group) :contact-group))

(defn save
  [group update?]
  (realm/save @realm/account-realm :contact-group group update?))

(defn save-property
  [group-id property-name value]
  (realm/write @realm/account-realm
               (fn []
                 (-> @realm/account-realm
                     (realm/get-by-field :contact-group :group-id group-id)
                     realm/single
                     (aset (name property-name) value)))))

(defn exists?
  [group-id]
  (realm/exists? @realm/account-realm :contact-group :group-id group-id))

(defn delete
  [group-id]
  (when-let [group (-> @realm/account-realm
                       (realm/get-by-field :contact-group :group-id group-id)
                       realm/single)]
    (realm/write @realm/account-realm #(realm/delete @realm/account-realm group))))

(defn- get-by-id-obj
  [group-id]
  (realm/single (realm/get-by-field @realm/account-realm :contact-group :group-id group-id)))

(defn add-contacts
  [group-id identities]
  (let [group    (get-by-id-obj group-id)
        contacts (object/get group "contacts")]
    (realm/write @realm/account-realm
                 #(aset group "contacts"
                        (clj->js (into #{} (concat identities
                                                   (realm/list->clj contacts))))))))
