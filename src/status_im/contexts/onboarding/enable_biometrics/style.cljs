(ns status-im.contexts.onboarding.enable-biometrics.style)

(def default-margin 20)

(def title-container
  {:margin-horizontal 20
   :padding-vertical  12})

(defn page-container
  [insets]
  {:flex            1
   :justify-content :space-between
   :padding-top     (+ (:top insets) 56)})

(defn page-illustration
  [width]
  {:flex           1
   :padding-top    12
   :padding-bottom 10
   :width          width})

(defn buttons
  [insets]
  {:margin        default-margin
   :margin-bottom (+ 12 (:bottom insets))})
