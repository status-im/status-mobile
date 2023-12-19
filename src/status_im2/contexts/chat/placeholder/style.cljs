(ns status-im2.contexts.chat.placeholder.style
  (:require [quo.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(defn container
  [top opacity z-index]
  (reanimated/apply-animations-to-style
   {:opacity opacity
    :z-index z-index}
   {:position         :absolute
    :padding-top      top
    :top              0
    :left             0
    :right            0
    :bottom           0
    :background-color (colors/theme-colors colors/white colors/neutral-95)}))
