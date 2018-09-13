(ns status-im.ui.components.connectivity.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]))

(defnstyle text-wrapper [top opacity window-width pending?]
  (cond->
   {:opacity          opacity
    :background-color colors/gray-notifications
    :height           35
    :position         :absolute}
    platform/desktop?
    (assoc
     :left             0
     :right            0)
    (not platform/desktop?)
    (assoc
     :ios              {:z-index 0}
     :width            window-width
     :top              (+ (+ 56 top) (if pending? 35 0)))))

(def text
  {:text-align :center
   :color      :white
   :font-size  14
   :top        8})
