(ns quo2.components.dividers.strength-divider.style)

(def container
  {:height             40
   :flex-direction     :row
   :padding-horizontal 20
   :align-items        :center})

(defn text
  [color]
  {:color       color
   :margin-left 4})

