(ns status-im.ui.components.sync-state.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]]))

(def sync-style-gradient
  {:position :relative
   :height   0
   :top      -2})

(defn loading-wrapper [opacity]
  {:background-color "#89b1fe"
   :opacity          opacity
   :height           2})

(defn gradient-wrapper [in-progress-opacity position]
  {:position :absolute
   :left     position
   :opacity  in-progress-opacity})

(defn gradient [width]
  {:width  width
   :height 2})

(defn synced-wrapper [opacity window-width]
  {:opacity          opacity
   :position         :absolute
   :width            window-width
   :background-color "#5fc48d"
   :height           2})

(defnstyle offline-wrapper [top opacity window-width pending?]
  {:ios              {:z-index 0}
   :opacity          opacity
   :width            window-width
   :top              (+ (+ 56 top) (if pending? 35 0))
   :position         :absolute
   :background-color "#828b92cc"
   :height           35})

(def offline-text
  {:text-align :center
   :color      :white
   :font-size  14
   :top        8})
