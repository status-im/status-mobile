(ns status-im.ui.components.connectivity.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]))

(defn text-wrapper
  [{:keys [window-width height background-color opacity transform]}]
  (cond-> {:flex-direction   :row
           :justify-content  :center
           :transform        [{:translateY transform}]
           :opacity          opacity
           :background-color (or background-color colors/gray)
           :height           height}

    platform/desktop?
    (assoc :left 0
           :right 0)

    (not platform/desktop?)
    (assoc :width window-width)))

(def text
  {:color      :white
   :font-size  14
   :top        8})
