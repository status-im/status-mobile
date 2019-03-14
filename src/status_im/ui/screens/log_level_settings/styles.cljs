(ns status-im.ui.screens.log-level-settings.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.styles :refer [defstyle]]))

(def wrapper
  {:flex             1
   :background-color :white})

(def log-level-item-inner
  {:padding-horizontal 16})

(defstyle log-level-item
  {:flex-direction     :row
   :background-color   :white
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(def log-level-item-name-text
  {:typography :title})

(def log-level-item-connected-text
  {:color      colors/gray
   :font-size  14
   :margin-top 6})

(defn log-level-icon-container [current?]
  {:width            40
   :height           40
   :border-radius    20
   :background-color (if current?
                       colors/blue
                       colors/gray-light)
   :align-items      :center
   :justify-content  :center})

(defn log-level-icon [current?]
  (hash-map (if platform/desktop? :tint-color :color)
            (if current? :white :gray)))
