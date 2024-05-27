(ns status-im.contexts.onboarding.enable-notifications.style)

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

(def page-title
  {:margin-top        12
   :margin-horizontal 20
   :margin-bottom     8})

(def page-heading {:z-index 1})

(defn buttons
  [insets]
  {:margin        default-margin
   :margin-bottom (+ 14 (:bottom insets))})
