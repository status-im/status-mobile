(ns status-im2.contexts.onboarding.enable-biometrics.style)

(def default-margin 20)

(defn page-container
  [insets]
  {:flex            1
   :justify-content :space-between
   :padding-top     (+ (:top insets) 56)})

(defn page-illustration
  [width]
  {:flex  1
   :width width})

(defn buttons
  [insets]
  {:margin        default-margin
   :margin-bottom (+ 12 (:bottom insets))})
