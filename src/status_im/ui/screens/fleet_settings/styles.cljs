(ns status-im.ui.screens.fleet-settings.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.styles :refer [defstyle]]))

(def wrapper
  {:flex             1
   :background-color :white})

(def fleet-item-inner
  {:padding-horizontal 16})

(defstyle fleet-item
  {:flex-direction     :row
   :background-color   :white
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(defstyle fleet-item-name-text
  {:color   colors/black
   :ios     {:font-size      17
             :letter-spacing -0.2
             :line-height    20}
   :desktop {:font-size 16}
   :android {:font-size 16}})

(defstyle fleet-item-connected-text
  {:color   colors/gray
   :ios     {:font-size      14
             :margin-top     6
             :letter-spacing -0.2}
   :android {:font-size  12
             :margin-top 2}})

(defn fleet-icon-container [current?]
  {:width            40
   :height           40
   :border-radius    20
   :background-color (if current?
                       colors/blue
                       colors/gray-light)
   :align-items      :center
   :justify-content  :center})

(defn fleet-icon [current?]
  (hash-map (if platform/desktop? :tint-color :color) (if current? :white :gray)))
