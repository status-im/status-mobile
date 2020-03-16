(ns status-im.ui.screens.fleet-settings.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]
            [status-im.utils.styles :as styles]))

(def wrapper
  {:flex             1})

(def fleet-item-inner
  {:padding-horizontal 16})

(styles/def fleet-item
  {:flex-direction     :row
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(def fleet-item-name-text
  {:font-size 17})

(def fleet-item-connected-text
  {:color      colors/gray
   :font-size  14
   :margin-top 6})

(defn fleet-icon-container [current?]
  {:width            40
   :height           40
   :border-radius    20
   :background-color (if current?
                       colors/blue
                       colors/black-transparent)
   :align-items      :center
   :justify-content  :center})

(defn fleet-icon [current?]
  (hash-map (if platform/desktop? :tint-color :color)
            (if current? colors/white-persist colors/gray)))
