(ns status-im2.contexts.activity-center.utils)

(defn contact-name
  [contact]
  (->> [(get-in contact [:names :nickname])
        (get-in contact [:names :ens-name])
        (get-in contact [:names :display-name])
        (get-in contact [:names :three-words-name])]

       (filter seq)
       first))
