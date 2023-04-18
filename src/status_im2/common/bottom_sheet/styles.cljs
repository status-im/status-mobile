(ns status-im2.common.bottom-sheet.styles
  (:require [quo2.foundations.colors :as colors]))

(defn handle
  [override-theme]
  {:width            32
   :height           4
   :background-color (colors/theme-colors colors/neutral-100 colors/white override-theme)
   :opacity          0.05
   :border-radius    100
   :align-self       :center
   :margin-vertical  8})

(defn sheet
  [{:keys [top bottom]} window-height override-theme]
  {:position                :absolute
   :max-height              (- window-height top 20)
   :z-index                 1
   :bottom                  0
   :left                    0
   :right                   0
   :border-top-left-radius  20
   :border-top-right-radius 20
   :flex                    1
   :padding-bottom          (max 20 bottom)
   :background-color        (colors/theme-colors colors/white colors/neutral-90 override-theme)})

(defn selected-item
  [override-theme]
  {:position          :absolute
   :bottom            10
   :left              0
   :right             0
   :border-radius     12
   :margin-horizontal 8
   :background-color  (colors/theme-colors colors/white colors/neutral-90 override-theme)})
