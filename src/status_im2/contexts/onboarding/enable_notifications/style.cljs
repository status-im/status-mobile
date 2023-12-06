(ns status-im2.contexts.onboarding.enable-notifications.style)

(def default-margin 20)

(defn page-container
  [insets]
  {:flex            1
   :justify-content :space-between
   :padding-top     (:top insets)})

(defn page-illustration
  [width]
  {:flex  1
   :width width})

(defn buttons
  [insets]
  {:margin        default-margin
   :margin-bottom (+ 14 (:bottom insets))})
