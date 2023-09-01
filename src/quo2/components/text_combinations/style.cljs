(ns quo2.components.text-combinations.style)

(defn container
  [container-style]
  (assoc container-style :margin-horizontal 20))

(def title-container
  {:flex-direction :row
   :align-items    :center})

(def avatar-container
  {:margin-right 9})

(def description-description-text
  {:margin-top 8})
