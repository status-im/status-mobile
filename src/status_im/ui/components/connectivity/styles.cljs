(ns status-im.ui.components.connectivity.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle]]))

(defnstyle offline-wrapper [top opacity window-width pending? second-line?]
  {:ios              {:z-index 0}
   :opacity          opacity
   :width            window-width
   :top              (+ (+ 56 top) (if pending? 35 0))
   :position         :absolute
   :background-color "#828b92cc"
   :height           (if second-line? 56 35)})

(def offline-text
  {:text-align :center
   :color      :white
   :font-size  14
   :top        8})
