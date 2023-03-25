(ns status-im2.contexts.onboarding.create-profile.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(def continue-button
  {:width         "100%"
   :margin-top    :auto
   :margin-bottom 72
   :margin-left   :auto
   :margin-right  :auto
   :align-self    :flex-end})

(def page-container
  {:padding-top      (if platform/ios? 44 0)
   :position         :absolute
   :top              0
   :bottom           0
   :left             0
   :right            0
   :background-color colors/neutral-80-opa-80-blur})

(def navigation-bar {:height 56})

(def info-message
  {:margin-top 8})
