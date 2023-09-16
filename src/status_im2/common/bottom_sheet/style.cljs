(ns status-im2.common.bottom-sheet.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.platform :as platform]))

(def bottom-margin 8)

(defn handle
  [theme]
  {:width            32
   :height           4
   :background-color (colors/theme-colors colors/neutral-100 colors/white theme)
   :opacity          (theme/theme-value 0.05 0.1 theme)
   :border-radius    100
   :align-self       :center
   :margin-vertical  8})

(defn sheet
  [{:keys [top bottom]} window-height theme padding-bottom-override selected-item shell?]
  {:position                :absolute
   :max-height              (- window-height top)
   :z-index                 1
   :bottom                  0
   :left                    0
   :right                   0
   :border-top-left-radius  20
   :border-top-right-radius 20
   :overflow                (when-not selected-item :hidden)
   :flex                    1
   :padding-bottom          (or padding-bottom-override (+ bottom 8))
   :background-color        (if shell?
                              :transparent
                              (colors/theme-colors colors/white colors/neutral-95 theme))})

(def gradient-bg
  {:position :absolute
   :top      0
   :left     0
   :right    0})

(def shell-bg
  {:position         :absolute
   :background-color (if platform/ios? colors/white-opa-5 colors/neutral-100-opa-90)
   :left             0
   :right            0
   :top              0
   :bottom           0})

(defn selected-item
  [theme max-height sheet-height show-bottom-margin]
  {:position          :absolute
   :top               (when (not show-bottom-margin) (- 0 max-height))
   ;; Bottom margin of 8 is added when the selected item height is less than 
   ;; or equals the max-height of the selected item
   :bottom            (when show-bottom-margin (+ sheet-height bottom-margin))
   :overflow          :hidden
   :left              0
   :right             0
   :border-radius     12
   :margin-horizontal 8
   :background-color  (colors/theme-colors colors/white colors/neutral-90 theme)})
