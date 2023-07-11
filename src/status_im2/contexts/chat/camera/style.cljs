(ns status-im2.contexts.chat.camera.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]))


(def flash-container
  {:position        :absolute
   :top             50
   :left            25})

(defn camera-window
  [width height top]
  {:width  width
   :height height
   :top    top})

(def zoom-button-container
  {:width           37
   :height          37
   :justify-content :center
   :align-items     :center})

(defn zoom-container
  [top insets]
  {:width            157
   :height           43
   :border-radius    100
   :position         :absolute
   :background-color colors/black-opa-60
   :align-self       :center
   :justify-content  :space-around
   :align-items      :center
   :flex-direction   :row
   :bottom           (+ top (:bottom insets) (when platform/android? (:top insets)) 18)})

(defn zoom-button
  [size]
  (reanimated/apply-animations-to-style
   {:width  size
    :height size}
   {:background-color colors/black-opa-30
    :justify-content  :center
    :align-items      :center
    :border-radius    50}))

(defn bottom-area
  [top insets]
  {:left     20
   :right    20
   :position :absolute
   :height   (+ top (when platform/android? (:top insets)))
   :bottom   (:bottom insets)})

(def photo-text
  {:color      colors/system-yellow
   :margin-top 18
   :font-size  14
   :align-self :center})

(def actions-container
  {:flex-direction  :row
   :margin-top      20
   :align-items     :center
   :justify-content :space-between})

(def outer-circle
  {:width            69
   :height           69
   :background-color colors/black
   :border-radius    69
   :border-width     6
   :border-color     colors/white
   :justify-content  :center
   :align-items      :center})

(def inner-circle
  {:width            53
   :height           53
   :border-radius    53
   :background-color colors/white})

(defn confirmation-container
  [insets]
  {:position           :absolute
   :bottom             0
   :left               0
   :right              0
   :background-color   "#131313"
   :height             (+ 69 (:bottom insets))
   :flex-direction     :row
   :padding-horizontal 20
   :justify-content    :space-between
   :padding-top        18})
