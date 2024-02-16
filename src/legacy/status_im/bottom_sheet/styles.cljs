(ns legacy.status-im.bottom-sheet.styles
  (:require
    [quo.foundations.colors :as colors]))

(def border-radius 20)

(defn handle
  [override-theme]
  {:position         :absolute
   :top              8
   :width            32
   :height           4
   :background-color (colors/theme-colors colors/neutral-100 colors/white override-theme)
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
  [insets bottom-safe-area-spacing?]
  {:position       :absolute
   :left           0
   :right          0
   :top            0
   :padding-top    border-radius
   :padding-bottom (if bottom-safe-area-spacing? (:bottom insets) 0)})

(defn selected-background
  [override-theme]
  {:border-radius     12
   :padding-left      12
   :margin-horizontal 8
   :margin-bottom     10
   :height            48
   :background-color  (colors/theme-colors colors/white colors/neutral-90 override-theme)})

(defn background
  [override-theme]
  {:background-color        (colors/theme-colors colors/white colors/neutral-95 override-theme)
   :flex                    1
   :border-top-left-radius  border-radius
   :border-top-right-radius border-radius})
