(ns status-im2.contexts.chat.photo-selector.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(defn gradient-container
  [safe-area]
  {:width    "100%"
   :height   (+ (:bottom safe-area) 65)
   :position :absolute
   :bottom   (if platform/ios? 0 65)})

(defn buttons-container
  [safe-area]
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-horizontal 20
   :bottom             (+ (:bottom safe-area) 33)})

(defn clear-container
  []
  {:background-color   (colors/theme-colors colors/neutral-10 colors/neutral-80)
   :position           :absolute
   :align-self         :flex-end
   :padding-horizontal 12
   :padding-vertical   5
   :right              20
   :border-radius      10})

(defn camera-button-container
  []
  {:background-color (colors/theme-colors colors/neutral-10 colors/neutral-80)
   :width            32
   :height           32
   :border-radius    10
   :justify-content  :center
   :align-items      :center
   :margin-left      20
   :margin-bottom    24})

(defn chevron-container
  []
  {:background-color (colors/theme-colors colors/neutral-10 colors/neutral-80)
   :width            14
   :height           14
   :border-radius    7
   :justify-content  :center
   :align-items      :center
   :margin-left      7
   :margin-top       4})

(defn image
  [window-width index]
  {:width         (- (/ window-width 3) 0.67)
   :height        (/ window-width 3)
   :margin-left   (when (not= (mod index 3) 0) 1)
   :margin-bottom 1})

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

