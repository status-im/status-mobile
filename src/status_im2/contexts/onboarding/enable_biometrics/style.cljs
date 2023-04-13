(ns status-im2.contexts.onboarding.enable-biometrics.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(def page-container
  {:padding-top      (if platform/ios? 44 0)
   :position         :absolute
   :top              0
   :bottom           0
   :left             0
   :right            0
   :background-color colors/neutral-80-opa-80-blur})

(def image-container
  {:margin-top       20
   :margin-bottom    24
   :background-color colors/danger-50
   :border-radius    20
   :flex             1
   :align-items      :center
   :justify-content  :center})
