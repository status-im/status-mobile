(ns status-im.contexts.onboarding.generating-keys.style)

(defn page-container
  [insets]
  {:flex        1
   :padding-top (:top insets)})

(defn page-illustration
  [width]
  {:flex  1
   :width width})

(def title-style
  {:position :absolute
   :left     0
   :right    0
   :top      0})
