(ns status-im.data-store.contact-recovery
  (:require [status-im.data-store.realm.core :as core]))

(defn get-contact-recovery-by-id [public-key]
  (-> @core/account-realm
      (core/get-by-field  :contact-recovery :id public-key)
      (core/single-clj :contact-recovery)))

(defn save-contact-recovery-tx
  "Returns tx function for saving a contact-recovery"
  [contact-recovery]
  (fn [realm]
    (core/create realm
                 :contact-recovery
                 contact-recovery
                 true)))
