(ns status-im.data-store.contact-recovery)

(defn get-contact-recovery-by-id [public-key])

(defn save-contact-recovery-tx
  "Returns tx function for saving a contact-recovery"
  [contact-recovery]
  (fn [realm]))
