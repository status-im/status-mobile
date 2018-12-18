(ns status-im.ui.components.connectivity.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]))

(defnstyle text-wrapper
  [{:keys [window-width modal? height background-color opacity]}]
  (cond-> {:flex-direction :row
           :justify-content :center
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
