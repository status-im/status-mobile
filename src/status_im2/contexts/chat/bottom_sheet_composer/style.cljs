(ns status-im2.contexts.chat.bottom-sheet-composer.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.bottom-sheet-composer.constants :as constants]))

(defn shadow
  [elevation?]
  (if platform/ios?
    {:shadow-radius  20
     :shadow-opacity (colors/theme-colors 0.1 0.7)
     :shadow-color   colors/neutral-100
     :shadow-offset  {:width 0 :height (colors/theme-colors -4 -8)}}
    {:elevation (if elevation? 10 0)}))

(defn sheet-container
  [insets opacity elevation?]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   (merge
    {:border-top-left-radius  20
     :border-top-right-radius 20
     :padding-horizontal      20
     :position                :absolute
     :bottom                  0
     :left                    0
     :right                   0
     :background-color        (colors/theme-colors colors/white colors/neutral-95)
     :z-index                 3
     :padding-bottom          (:bottom insets)}
    (shadow elevation?))))

(def bar-container
  {:height          constants/bar-container-height
   :left            0
   :right           0
   :top             0
   :z-index         1
   :justify-content :center
   :align-items     :center})

(defn bar
  []
  {:width            32
   :height           4
   :border-radius    100
   :background-color (colors/theme-colors colors/neutral-100-opa-5 colors/white-opa-10)})

(defn input-container
  [height max-height]
  (reanimated/apply-animations-to-style
   {:height height}
   {:max-height max-height
    :overflow   :hidden}))

(defn input
  [maximized? saved-keyboard-height]
  (merge typography/paragraph-1
         {:min-height          constants/input-height
          :color               (colors/theme-colors :black :white)
          :text-align-vertical :top
          :flex                1
          :z-index             1
          :position            (if saved-keyboard-height :relative :absolute)
          :top                 0
          :left                0
          :right               (when (or maximized? platform/ios?) 0)}))

(defn background
  [opacity background-y window-height]
  (reanimated/apply-animations-to-style
   {:opacity   opacity
    :transform [{:translate-y background-y}]}
   {:position         :absolute
    :left             0
    :right            0
    :bottom           0
    :height           window-height
    :background-color colors/neutral-95-opa-70
    :z-index          1}))

(defn blur-container
  [height]
  (reanimated/apply-animations-to-style
   {:height height}
   {:position                :absolute
    :elevation               10
    :left                    0
    :right                   0
    :bottom                  0
    :border-top-right-radius 20
    :border-top-left-radius  20
    :overflow                :hidden}))

(defn blur-view
  []
  {:style       {:flex 1}
   :blur-radius (if platform/ios? 20 10)
   :blur-type   (colors/theme-colors :light :dark)
   :blur-amount 20})
