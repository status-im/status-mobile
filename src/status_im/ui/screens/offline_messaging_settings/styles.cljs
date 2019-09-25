(ns status-im.ui.screens.offline-messaging-settings.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]
            [status-im.utils.styles :as styles]))

(def wrapper
  {:flex             1
   :background-color :white})

(def mailserver-item-inner
  {:padding-horizontal 16})

(styles/defn mailserver-item [pinned?]
  {:flex-direction     :row
   :background-color   :white
   :align-items        :center
   :opacity            (if pinned?
                         1
                         0.4)
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(def mailserver-item-name-text
  {:typography :title})

(defn mailserver-icon-container [connected?]
  {:width            40
   :height           40
   :border-radius    20
   :background-color (if connected?
                       colors/blue
                       colors/black-transparent)
   :align-items      :center
   :justify-content  :center})

(defn mailserver-icon [connected?]
  (hash-map (if platform/desktop? :tint-color :color)
            (if connected? :white :gray)))

(def mailserver-pinned
  {:padding-horizontal 16
   :flex-direction :row
   :padding-vertical 5})

(def mailserver-pinned-text-container
  {:margin-top 15})
