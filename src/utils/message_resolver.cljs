(ns status-im2.utils.message-resolver
  (:require [utils.re-frame :as rf]))

(defn resolve-message
  [parsed-text]
  (reduce
   (fn [acc {:keys [type literal destination] :as some-text}]
     (str acc
          (case type
            "paragraph"
            (resolve-message (:children some-text))

            "mention"
            (rf/sub [:messages/resolve-mention literal])

            "status-tag"
            (str "#" literal)

            "link"
            destination

            literal)))
   ""
   parsed-text))
