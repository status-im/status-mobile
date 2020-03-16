(ns status-im.ui.screens.log-level-settings.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]
            [status-im.utils.styles :as styles]))

(def wrapper
  {:flex             1})

(def log-level-item-inner
  {:padding-horizontal 16})

(styles/def log-level-item
  {:flex-direction     :row
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(def log-level-item-name-text
  {:typography :title})

(defn log-level-icon-container [current?]
  {:width            40
   :height           40
   :border-radius    20
   :background-color (if current?
                       colors/blue
                       colors/black-transparent)
   :align-items      :center
   :justify-content  :center})

(defn log-level-icon [current?]
  (hash-map (if platform/desktop? :tint-color :color)
            (if current? colors/white-persist colors/gray)))
