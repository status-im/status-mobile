(ns status-im2.contexts.onboarding.welcome.style
  (:require
    [react-native.platform :as platform]
    [quo2.foundations.colors :as colors]))

(def title-container
  {:justify-content    :center
   :margin-top         12
   :padding-horizontal 20})

(def welcome-container
  {:flex             1
   :padding-top      (if platform/ios? 44 0)
   :background-color colors/neutral-80-opa-80-blur})

(def page-illustration
  {:flex              1
   :background-color  colors/danger-50
   :align-items       :center
   :margin-horizontal 20
   :border-radius     20
   :margin-top        20
   :justify-content   :center})

