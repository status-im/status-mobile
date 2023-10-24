(ns status-im2.contexts.onboarding.welcome.style)

(def default-margin 20)

(defn page-container
  [insets]
  {:flex        1
   :padding-top (:top insets)})

(def page-illustration
  {:resize-mode   :stretch
   :resize-method :scale
   :margin-top    12
   :margin-bottom 4})

(defn buttons
  [insets]
  {:margin        default-margin
   :margin-bottom (+ 14 (:bottom insets))})
