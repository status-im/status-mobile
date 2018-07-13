(ns status-im.ui.screens.extensions.styles
  (:require [status-im.ui.components.colors :as colors]))

(def wrapper
  {:flex             1
   :background-color colors/white})

(defn wnode-icon [connected?]
  {:width            40
   :height           40
   :border-radius    20
   :background-color (if connected?
                       colors/blue
                       colors/gray-light)
   :align-items      :center
   :justify-content  :center})

(def empty-list
  {:color      colors/black
   :text-align :center})

