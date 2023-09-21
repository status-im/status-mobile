(ns status-im2.contexts.onboarding.common.overlay.style
  (:require [react-native.reanimated :as reanimated]
            [quo2.foundations.colors :as colors]))

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
   :background-color colors/neutral-80-opa-80-blur})
