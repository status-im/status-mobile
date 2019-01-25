(ns status-im.ui.components.bottom-sheet.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]))

(def border-radius 16)
(def bottom-padding (if platform/iphone-x? 34 8))
(def bottom-view-height 1000)

(def container
  {:position        :absolute
   :left            0
   :top             0
   :right           0
   :bottom          0
   :flex            1
   :justify-content :flex-end})

(defn shadow [opacity-value]
  {:flex             1
   :position         :absolute
   :left             0
   :top              0
   :right            0
   :bottom           0
   :opacity          opacity-value
   :background-color colors/black-transparent-40})

(defn content-container
  [content-height bottom-value]
  {:background-color        colors/white
   :border-top-left-radius  border-radius
   :border-top-right-radius border-radius
   :height                  (+ content-height border-radius bottom-view-height)
   :bottom                  (- bottom-view-height)
   :align-self              :stretch
   :transform               [{:translateY bottom-value}]
   :justify-content         :flex-start
   :align-items             :center
   :padding-bottom          bottom-padding})

(def content-header
  {:height          border-radius
   :align-self      :stretch
   :justify-content :center
   :align-items     :center})

(def handle
  {:width            31
   :height           4
   :background-color colors/gray-transparent-40
   :border-radius    2})

(def bottom-view
  {:background-color colors/white
   :height           bottom-view-height
   :align-self       :stretch})
