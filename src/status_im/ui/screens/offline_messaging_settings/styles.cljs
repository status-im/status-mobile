(ns status-im.ui.screens.offline-messaging-settings.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.styles :refer [defstyle]]))

(def wrapper
  {:flex             1
   :background-color :white})

(def wnode-item-inner
  {:padding-horizontal 16})

(defstyle wnode-item
  {:flex-direction     :row
   :background-color   :white
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(defstyle wnode-item-name-text
  {:color   colors/black
   :ios     {:font-size      17
             :letter-spacing -0.2
             :line-height    20}
   :desktop {:font-size 16}
   :android {:font-size 16}})

(defstyle wnode-item-connected-text
  {:color   colors/gray
   :ios     {:font-size      14
             :margin-top     6
             :letter-spacing -0.2}
   :android {:font-size  12
             :margin-top 2}})

(defn wnode-icon-container [connected?]
  {:width            40
   :height           40
   :border-radius    20
   :background-color (if connected?
                       colors/blue
                       colors/gray-light)
   :align-items      :center
   :justify-content  :center})

(defn wnode-icon [connected?]
  (hash-map (if platform/desktop? :tint-color :color) (if connected? :white :gray)))
