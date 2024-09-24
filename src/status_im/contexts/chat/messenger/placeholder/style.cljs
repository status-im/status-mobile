(ns status-im.contexts.chat.messenger.placeholder.style
  (:require [quo.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(defn container
  [top opacity z-index theme]
  (reanimated/apply-animations-to-style
   {:opacity opacity
    :z-index z-index}
   {:position         :absolute
    :padding-top      top
    :top              0
    :left             0
    :right            0
    :bottom           0
    :background-color (colors/theme-colors colors/white colors/neutral-95 theme)}))
