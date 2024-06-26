(ns status-im.contexts.onboarding.common.background.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(def background-container
  {:background-color colors/neutral-100
   :flex-direction   :row
   :position         :absolute
   :overflow         :hidden
   :top              0
   :bottom           0
   :left             0
   :right            0})

(def background-blur-overlay
  {:position         :absolute
   :left             0
   :top              0
   :bottom           0
   :right            0
   :background-color (when platform/ios? colors/neutral-80-opa-80-blur)})
