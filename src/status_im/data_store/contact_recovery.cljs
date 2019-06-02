(ns status-im.data-store.contact-recovery
  (:require [status-im.data-store.realm.core :as core]))

(defn get-contact-recovery-by-id [public-key]
  (core/realm-obj->clj (.objectForPrimaryKey @core/account-realm
                                             "contact-recovery"
                                             public-key)
                       :contact-recovery))

(defn save-contact-recovery-tx
  "Returns tx function for saving a contact-recovery"
  [contact-recovery]
  (fn [realm]
    (core/create realm
                 :contact-recovery
                 contact-recovery
                 true)))
