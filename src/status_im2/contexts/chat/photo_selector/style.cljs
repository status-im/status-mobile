(ns status-im2.contexts.chat.photo-selector.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(defn gradient-container
  [insets]
  {:left     0
   :right    0
   :height   (+ (:bottom insets) (if platform/ios? 65 85))
   :position :absolute
   :bottom   0})

(def buttons-container
  {:position        :absolute
   :flex-direction  :row
   :left            0
   :right           0
   :margin-top      20
   :margin-bottom   12
   :justify-content :center
   :z-index         1})

(defn clear-container
  []
  {:background-color   (colors/theme-colors colors/neutral-10 colors/neutral-80)
   :padding-horizontal 12
   :padding-vertical   5
   :border-radius      10
   :position           :absolute
   :right              20})

(defn camera-button-container
  []
  {:background-color (colors/theme-colors colors/neutral-10 colors/neutral-80)
   :width            32
   :height           32
   :border-radius    10
   :justify-content  :center
   :align-items      :center
   :position         :absolute
   :left             20})

(def title-container
  {:flex-direction     :row
   :background-color   (colors/theme-colors colors/neutral-10 colors/neutral-80)
   :border-radius      10
   :padding-horizontal 12
   :padding-vertical   5
   :align-self         :center})

(defn chevron-container
  []
  {:background-color (colors/theme-colors colors/neutral-30 colors/neutral-100)
   :width            14
   :height           14
   :border-radius    7
   :justify-content  :center
   :align-items      :center
   :margin-left      7
   :margin-top       4})

(defn image
  [window-width index]
  {:width                   (- (/ window-width 3) 0.67)
   :height                  (/ window-width 3)
   :margin-left             (when (not= (mod index 3) 0) 1)
   :margin-bottom           1
   :border-top-left-radius  (when (= index 0) 10)
   :border-top-right-radius (when (= index 2) 10)})

(defn overlay
  [window-width]
  {:position         :absolute
   :width            (- (/ window-width 3) 0.67)
   :height           (/ window-width 3)
   :background-color (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)})

(def image-count
  {:width         24
   :height        24
   :border-radius 8
   :top           8
   :right         8})

