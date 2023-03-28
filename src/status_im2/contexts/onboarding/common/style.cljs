(ns status-im2.contexts.onboarding.common.style
  (:require
    [quo2.foundations.colors :as colors]))

(def default-margin 20)

(defn page-container
  [insets]
  {:flex             1
   :padding-top      (:top insets)
   :background-color colors/neutral-80-opa-80-blur})

(def title-container
  {:justify-content    :center
   :margin-top         12
   :padding-horizontal default-margin})

(def title-text
  {:color         colors/white
   :margin-bottom 8})

(def regular-text
  {:color colors/white})

(def page-illustration
  {:flex              1
   :background-color  colors/danger-50
   :align-items       :center
   :margin-horizontal default-margin
   :border-radius     20
   :margin-top        default-margin
   :justify-content   :center})

(defn buttons
  [insets]
  {:margin        default-margin
   :margin-bottom (+ default-margin (:bottom insets))})