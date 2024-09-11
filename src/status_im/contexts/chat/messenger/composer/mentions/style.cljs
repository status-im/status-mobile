(ns status-im.contexts.chat.messenger.composer.mentions.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.composer.constants :as constants]))


(defn shadow
  [theme]
  (if platform/ios?
    {:shadow-radius  (colors/theme-colors 30 50 theme)
     :shadow-opacity (colors/theme-colors 0.1 0.7 theme)
     :shadow-color   colors/neutral-100
     :shadow-offset  {:width 0 :height (colors/theme-colors 8 12 theme)}}
    {:elevation 10}))

(defn container
  [opacity bottom theme]
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
     :background-color (colors/theme-colors colors/white colors/neutral-95 theme)}
    (shadow theme))))
