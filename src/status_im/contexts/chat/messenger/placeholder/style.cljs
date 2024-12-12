(ns status-im.contexts.chat.messenger.placeholder.style
  (:require [quo.foundations.colors :as colors]
            [react-native.safe-area :as safe-area]))

(defn container
  [theme on-layout-done?]
  {:position         :absolute
   :padding-top      (safe-area/get-top)
   :top              0
   :left             0
   :right            0
   :bottom           0
   :background-color (colors/theme-colors colors/white colors/neutral-95 theme)
   :opacity          (if on-layout-done? 0 1)
   :z-index          (if on-layout-done? 0 2)})
