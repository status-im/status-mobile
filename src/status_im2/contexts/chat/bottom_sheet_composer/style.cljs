(ns status-im2.contexts.chat.bottom-sheet-composer.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.bottom-sheet-composer.constants :as c]))


(def shadow
  (if platform/ios?
    {:shadow-radius  20
     :shadow-opacity 0.1
     :shadow-color   "#09101C"
     :shadow-offset  {:width 0 :height -4}}
    {:elevation 10}))

(defn sheet-container
  [insets opacity]
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
    shadow)))

(defn bar-container
  []
  {:height          c/bar-container-height
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
         {:min-height          c/input-height
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

(def blur-view
  {:style       {:flex 1}
   :blur-radius 20
   :blur-type   :light
   :blur-amount 20})

(defn top-gradient-style
  [opacity z-index]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:height   80
    :position :absolute
    :z-index  z-index
    :top      0
    :left     0
    :right    0}))

(defn top-gradient
  [opacity z-index]
  {:colors [(colors/theme-colors colors/white-opa-0 colors/neutral-95-opa-0)
            (colors/theme-colors colors/white colors/neutral-95)]
   :start  {:x 0 :y 1}
   :end    {:x 0 :y 0}
   :style  (top-gradient-style opacity z-index)})

(def bottom-gradient-style
  {:height   (if platform/ios? (:line-height typography/paragraph-1) 32)
   :position :absolute
   :bottom   0
   :left     0
   :right    0
   :z-index  2})

(defn bottom-gradient
  []
  {:colors [(colors/theme-colors colors/white colors/neutral-95)
            (colors/theme-colors colors/white-opa-0 colors/neutral-95-opa-0)]
   :start  {:x 0 :y 1}
   :end    {:x 0 :y 0}
   :style  bottom-gradient-style})
