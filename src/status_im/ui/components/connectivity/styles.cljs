(ns status-im.ui.components.connectivity.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(defnstyle text-wrapper [top opacity window-width pending?]
  {:ios              {:z-index 0}
   :opacity          opacity
   :width            window-width
   :top              (+ (+ 56 top) (if pending? 35 0))
   :position         :absolute
   :background-color colors/gray-notifications
   :height           35})

(def text
  {:text-align :center
   :color      :white
   :font-size  14
   :top        8})
