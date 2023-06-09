(ns status-im2.contexts.onboarding.syncing.results.style
  (:require [quo2.foundations.colors :as colors]))

(defn page-container
  [top]
  {:flex             1
   :position         :absolute
   :top              0
   :bottom           0
   :left             0
   :right            0
   :padding-top      top
   :padding-bottom   20
   :background-color colors/neutral-80-opa-80-blur})

(def current-device
  {:margin-bottom 19})

(def device-list
  {:flex               1
   :margin-top         12
   :padding-horizontal 20})

(def continue-button
  {:margin-top         20
   :padding-horizontal 20})
