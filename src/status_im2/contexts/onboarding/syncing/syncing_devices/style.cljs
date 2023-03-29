(ns status-im2.contexts.onboarding.syncing.syncing-devices.style
  (:require [quo2.foundations.colors :as colors]))

(def page-container
  {:position         :absolute
   :top              0
   :bottom           0
   :left             0
   :right            0
   :padding-bottom   20
   :background-color colors/neutral-80-opa-80-blur})

(def page-illustration
  {:flex              1
   :background-color  colors/danger-50
   :align-items       :center
   :margin-horizontal 20
   :border-radius     20
   :margin-top        20
   :justify-content   :center})