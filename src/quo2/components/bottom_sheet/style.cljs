(ns quo2.components.bottom-sheet.style
  (:require
   [quo.design-system.spacing :as spacing]
   [quo2.foundations.colors :as quo2.colors]))

(def border-radius 16)
(def vertical-padding (:tiny spacing/spacing))
(def margin-top 56)

(def container
  {:position        :absolute
   :left            0
   :top             0
   :right           0
   :bottom          0
   :flex            1
   :justify-content :flex-end})

(defn backdrop []
  {:flex             1
   :position         :absolute
   :left             0
   :top              0
   :right            0
   :bottom           0})

(defn content-container
  [window-height]
  {:background-color        (quo2.colors/theme-colors
                             quo2.colors/white
                             quo2.colors/neutral-90)
   :border-top-left-radius  border-radius
   :border-top-right-radius border-radius
   :height                  (* window-height 2)})

(def content-header
  {:height          border-radius
   :align-self      :stretch
   :justify-content :center
   :align-items     :center})

(def handle
  {:width            31
   :height           4
   :background-color (quo2.colors/theme-colors
                      quo2.colors/neutral-50
                      quo2.colors/neutral-40)
   :opacity          0.4
   :border-radius    2})
