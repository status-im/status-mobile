(ns status-im2.contexts.chat.composer.mentions.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.composer.constants :as constants]))


(defn shadow
  []
  (if platform/ios?
    {:shadow-radius  (colors/theme-colors 30 50)
     :shadow-opacity (colors/theme-colors 0.1 0.7)
     :shadow-color   colors/neutral-100
     :shadow-offset  {:width 0 :height (colors/theme-colors 8 12)}}
    {:elevation 10}))

(defn container
  [opacity bottom]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   (merge
    {:position         :absolute
     :bottom           bottom
     :left             8
     :right            8
     :border-radius    16
     :z-index          4
     :max-height       constants/mentions-max-height
     :background-color (colors/theme-colors colors/white colors/neutral-95)}
    (shadow))))
