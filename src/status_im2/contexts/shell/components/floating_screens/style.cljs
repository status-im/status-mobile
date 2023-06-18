(ns status-im2.contexts.shell.components.floating-screens.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(defn screen
  [{:keys [screen-left screen-top screen-width screen-height screen-z-index]}]
  (reanimated/apply-animations-to-style
   {:left    screen-left
    :top     screen-top
    :width   screen-width
    :height  screen-height
    :z-index screen-z-index}
   {:background-color (colors/theme-colors colors/white colors/neutral-95)
    :overflow         :hidden
    :position         :absolute}))

(defn screen-container
  [{:keys [width height]}]
  {:width  width
   :height height})
