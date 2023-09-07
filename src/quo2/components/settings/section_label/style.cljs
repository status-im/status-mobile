(ns quo2.components.settings.section-label.style
  (:require
    [quo2.foundations.typography :as typography]))

(defn section
  [color size]
  (-> (case size
            :small
            typography/paragraph-2
            :medium
            typography/paragraph-1)
      (merge typography/font-medium)
      (assoc :color       color
             :font-weight "500")))

(defn description
  [color]
  (->
    typography/paragraph-1
    (merge typography/font-regular)
    (assoc :color       color
           :font-weight "400")))
