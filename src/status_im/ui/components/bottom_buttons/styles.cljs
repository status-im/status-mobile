(ns status-im.ui.components.bottom-buttons.styles
  (:require [status-im.ui.components.colors :as colors]))

(def wrapper
  {:position :absolute
   :bottom   0
   :left     0
   :right    0})

(def border
  {:margin-horizontal 16
   :border-top-width  1
   :border-color      colors/white-light-transparent})

(def container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def container-single
  {:flex-direction  :row
   :align-items     :center
   :justify-content :center})
