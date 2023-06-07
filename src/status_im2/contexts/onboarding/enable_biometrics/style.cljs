(ns status-im2.contexts.onboarding.enable-biometrics.style
  (:require [quo2.foundations.colors :as colors]))

(def default-margin 20)

(defn page-container
  [insets]
  {:flex             1
   :justify-content  :space-between
   :padding-top      (:top insets)
   :background-color colors/neutral-80-opa-80-blur})

(defn buttons
  [insets]
  {:margin        default-margin
   :margin-bottom (+ 46 (:bottom insets))})
