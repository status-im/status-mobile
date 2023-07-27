(ns status-im2.contexts.onboarding.generating-keys.style)

(defn page-container
  [insets]
  {:flex            1
   :justify-content :space-between
   :padding-top     (:top insets)})

(defn page-illustration
  [width]
  {:flex  1
   :width width})
