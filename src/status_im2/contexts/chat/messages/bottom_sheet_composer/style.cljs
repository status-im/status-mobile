(ns status-im2.contexts.chat.messages.bottom-sheet-composer.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.messages.bottom-sheet-composer.constants :as c]))

(def shadow
  (if platform/ios?
    {:shadow-radius  20
     :shadow-opacity 0.1
     :shadow-color   "#09101C"
     :shadow-offset  {:width 0 :height -4}}
    {:elevation 10}))

(defn container
  [insets focused? text? images?]
  (merge
    {:border-top-left-radius  20
     :border-top-right-radius 20
     :padding-horizontal      20
     :position                :absolute
     :bottom                  0
     :left                    0
     :right                   0
     :background-color        (colors/theme-colors colors/white colors/neutral-90)
     :opacity                 (if (or focused? text? images?) 1 (if platform/ios? 0.7 0.5))
     :z-index                 3
     :padding-bottom          (:bottom insets)}
    shadow))

(defn handle-container
  []
  {:height          c/handle-container-height
   :left            0
   :right           0
   :top             0
   :z-index         1
   :justify-content :center
   :align-items     :center})

(defn handle
  []
  {:width            32
   :height           4
   :border-radius    100
   :background-color (colors/theme-colors colors/neutral-100-opa-5 colors/white-opa-10)})

(defn input
  [expanded? saved-keyboard-height]
  (merge typography/paragraph-1
         {:min-height          c/input-height
          :color               (colors/theme-colors :black :white)
          :text-align-vertical :top
          :flex                1
          :position            (if saved-keyboard-height :relative :absolute)
          :top                 0
          :left                0
          ; to inc gesture detection area on Android
          :right               (when (or expanded? platform/ios?) 0)}))

(defn input-container
  [height max-height]
  (reanimated/apply-animations-to-style
    {:height height}
    {
     ;:min-height c/input-height
     :max-height max-height
     :overflow   :hidden}))


(defn actions-container
  []
  {:height          c/actions-container-height
   :justify-content :space-between
   :align-items     :center
   :z-index         2
   :flex-direction  :row})

(defn background
  [opacity translate-y window-height]
  (reanimated/apply-animations-to-style
    {:opacity   opacity
     :transform [{:translate-y translate-y}]}
    {:position         :absolute
     :left             0
     :right            0
     :bottom           0
     :height           window-height
     :background-color colors/neutral-95-opa-70
     :z-index          1}))

(defn blur-container
  [opacity height]
  (reanimated/apply-animations-to-style
    {:opacity opacity
     :height  height}
    {:position                :absolute
     :elevation               10
     :left                    0
     :right                   0
     :bottom                  0
     :border-top-right-radius 20
     :border-top-left-radius  20
     :overflow                :hidden}))

(defn text-top-gradient
  [opacity z-index]
  (reanimated/apply-animations-to-style
    {:opacity opacity}
    {:height   c/top-gradient-height
     :position :absolute
     :z-index  z-index
     :top      0
     :left     0
     :right    0}))

(defn text-bottom-gradient
  []
  {:height   (if platform/ios? (:line-height typography/paragraph-1) 32)
   :position :absolute
   :bottom   0
   :left     0
   :right    0})
