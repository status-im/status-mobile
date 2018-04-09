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

(defn get-by-id-obj
  [group-id]
  (-> @realm/account-realm
      (realm/get-one-by-field :contact-group :group-id group-id)))

(defn add-contacts
  [group-id identities]
  (let [group    (get-by-obj-id group-id)
        contacts (object/get group "contacts")]
    (realm/write @realm/account-realm
                 #(aset group "contacts"
                        (clj->js (into #{} (concat identities
                                                   (realm/js-object->clj contacts))))))))
