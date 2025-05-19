(ns status-im.contexts.onboarding.preparing-status.style)

(defn page-container
  [insets]
  {:flex        1
   :padding-top (:top insets)})

(defn page-illustration
  [width]
  {:flex  1
   :width width})
