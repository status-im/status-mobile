(ns status-im2.contexts.chat.lightbox.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]))

(def container-view
  {:background-color :black
   :height           "100%"})

(defn top-view-container
  [top-inset opacity]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:position       :absolute
    :left           20
    :top            (if platform/ios? (+ 12 top-inset) 12)
    :z-index        4
    :flex-direction :row
    :width          "100%"}))


(def close-container
  {:width            32
   :height           32
   :border-radius    12
   :justify-content  :center
   :align-items      :center
   :background-color colors/neutral-80-opa-40})

(def top-right-buttons
  {:position       :absolute
   :right          40
   :flex-direction :row})

(defn gradient-container
  [insets opacity]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:width    "100%"
    :position :absolute
    :bottom   (:bottom insets)
    :z-index  3}))

(def text-style
  {:color             colors/white
   :align-self        :center
   :margin-horizontal 20
   :margin-vertical   12})
