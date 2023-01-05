(ns status-im2.common.bottom-sheet.styles
  (:require [quo2.foundations.colors :as colors]))

(def border-radius 20)

(defn handle
  []
  {:position         :absolute
   :top              8
   :width            32
   :height           4
   :background-color (colors/theme-colors colors/neutral-100 colors/white)
   :opacity          0.1
   :border-radius    100
   :align-self       :center})

(def backdrop
  {:position         :absolute
   :left             0
   :right            0
   :bottom           0
   :top              0
   :background-color colors/neutral-100})

(def container
  {:position :absolute
   :left     0
   :right    0
   :top      0
   :bottom   0
   :overflow :hidden})

(defn content-style
  [insets]
  {:position       :absolute
   :left           0
   :right          0
   :top            0
   :padding-top    border-radius
   :padding-bottom (:bottom insets)})

(defn selected-background
  []
  {:border-radius     12
   :padding-left      12
   :margin-horizontal 8
   :margin-bottom     10
   :height            48
   :background-color  (colors/theme-colors colors/white colors/neutral-90)})

(defn background
  []
  {:background-color        (colors/theme-colors colors/white colors/neutral-95)
   :flex                    1
   :border-top-left-radius  border-radius
   :border-top-right-radius border-radius})
