(ns status-im.ui.components.bottom-sheet.styles
  (:require [status-im.ui.components.colors :as colors]))

(def border-radius 16)
(def vertical-padding 8)
(def margin-top 56)

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
   :background-color colors/black-transparent-40-persist})

(defn content-container
  [window-height content-height bottom-value]
  {:background-color        colors/white
   :border-top-left-radius  border-radius
   :border-top-right-radius border-radius
   :height                  (+ content-height window-height)
   :bottom                  (- window-height)
   :transform               [{:translateY bottom-value}]})

(def sheet-wrapper {:flex            1
                    :justify-content :flex-end})

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
