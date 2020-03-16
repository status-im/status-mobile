(ns status-im.ui.components.connectivity.styles
  (:require [status-im.ui.components.colors :as colors]))

(defn text-wrapper
  [{:keys [window-width height background-color opacity transform]}]
  {:flex-direction   :row
   :justify-content  :center
   :transform        [{:translateY transform}]
   :opacity          opacity
   :background-color (or background-color colors/gray)
   :height           height
   :width            window-width})

(def text
  {:color     colors/white
   :font-size 14
   :top       8})