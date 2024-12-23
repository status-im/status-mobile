(ns status-im.contexts.onboarding.common.background.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(def background-container
  {:background-color colors/neutral-100
   :flex             1
   :overflow         :hidden})

(defn background-image-container
  [width]
  {:flex           1
   :flex-direction :row
   :width          width})

(def background-blur-overlay
  {:position         :absolute
   :left             0
   :top              0
   :bottom           0
   :right            0
   :background-color (when platform/ios? colors/neutral-80-opa-80-blur)})
