(ns status-im2.contexts.onboarding.syncing.results.style
  (:require [quo2.foundations.colors :as colors]))

(def page-container
  {:flex             1
   :position         :absolute
   :top              0
   :bottom           0
   :left             0
   :right            0
   :padding-bottom   20
   :background-color colors/neutral-80-opa-80-blur})

(def current-device
  {:flex 1})

(def device-list
  {:flex               1
   :margin-top         24
   :padding-horizontal 20})

(def continue-button
  {:margin-top         20
   :padding-horizontal 20})
