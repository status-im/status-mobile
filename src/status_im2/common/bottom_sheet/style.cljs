(ns status-im2.common.bottom-sheet.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [status-im.utils.platform :as platform]))

(defn handle
  [override-theme]
  {:width            32
   :height           4
   :background-color (colors/theme-colors colors/neutral-100 colors/white override-theme)
   :opacity          (theme/theme-value 0.05 0.1)
   :border-radius    100
   :align-self       :center
   :margin-vertical  8})

(defn sheet
  [{:keys [top bottom]} window-height override-theme padding-bottom-override shell?]
  {:position                :absolute
   :max-height              (- window-height top)
   :z-index                 1
   :bottom                  0
   :left                    0
   :right                   0
   :border-top-left-radius  20
   :border-top-right-radius 20
   :overflow                (when shell? :hidden)
   :flex                    1
   :padding-bottom          (or padding-bottom-override (+ bottom 8))
   :background-color        (if shell?
                              :transparent
                              (colors/theme-colors colors/white colors/neutral-90 override-theme))})

(def shell-bg
  {:position         :absolute
   :background-color (if platform/ios? colors/white-opa-5 colors/neutral-100-opa-90)
   :left             0
   :right            0
   :top              0
   :bottom           0})

(defn selected-item
  [override-theme window-height sheet-height {:keys [top]}]
  {:position          :absolute
   :bottom            10
   :max-height        (- window-height sheet-height top)
   :overflow          :hidden
   :left              0
   :right             0
   :border-radius     12
   :margin-horizontal 8
   :background-color  (colors/theme-colors colors/white colors/neutral-90 override-theme)})
