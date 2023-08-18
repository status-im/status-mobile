(ns quo2.components.text-combinations.style)

(defn container
  [container-style]
  (merge
   {:flex              1
    :margin-horizontal 20}
   container-style))

(def title-container
  {:flex           1
   :flex-direction :row
   :align-items    :center})

(def avatar-container
  {:margin-right 9})

(def description-description-text
  {:margin-top 8})
