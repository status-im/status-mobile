(ns status-im.ui.screens.status.new.styles
  (:require [status-im.ui.components.colors :as colors]))

(def buttons
  {:padding-horizontal 14
   :padding-vertical   10
   :justify-content    :space-between
   :height             88})

(def image
  {:width            72
   :height           72
   :background-color :black
   :resize-mode      :cover
   :margin-right     4
   :border-radius    4})

(defn photos-buttons []
  {:height           88
   :border-top-width 1
   :border-top-color colors/gray-lighter
   :flex-direction   :row
   :align-items      :center})

(def count-container
  {:top             0
   :bottom          0
   :left            0
   :right           0
   :align-items     :center
   :justify-content :center
   :position        :absolute
   :pointerEvents   :none})