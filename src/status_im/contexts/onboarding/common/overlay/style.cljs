(ns status-im.contexts.onboarding.common.overlay.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]))

(defn blur-container
  [opacity]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:position :absolute
    :top      0
    :left     0
    :right    0
    :bottom   0}))

(def blur-style
  {:flex             1
   :background-color (when platform/ios? colors/neutral-80-opa-80-blur)})
