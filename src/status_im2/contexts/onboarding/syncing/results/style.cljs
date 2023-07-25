(ns status-im2.contexts.onboarding.syncing.results.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

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

(defn content
  [translate-x]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x translate-x}]}
   {:margin-top    56
    :margin-bottom 26
    :flex          1}))

(def current-device
  {:margin-bottom 19})

(def device-list
  {:flex               1
   :margin-top         12
   :padding-horizontal 20})

(def continue-button
  {:margin-top         20
   :padding-horizontal 20})
