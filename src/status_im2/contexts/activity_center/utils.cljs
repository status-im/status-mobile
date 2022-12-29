(ns status-im2.contexts.activity-center.utils)

(defn contact-name
  [contact]
  (or (get-in contact [:names :nickname])
      (get-in contact [:names :three-words-name])))
