(ns status-im.ui.screens.offline-messaging-settings.styles
  (:require [status-im.ui.components.styles :as common])
  (:require-macros [status-im.utils.styles :refer [defstyle]]))

(def wnodes-list
  {:background-color common/color-light-gray})

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
  {:color   common/color-black
   :ios     {:font-size      17
             :letter-spacing -0.2
             :line-height    20}
   :android {:font-size 16}})

(defstyle wnode-item-connected-text
  {:color   common/color-gray4
   :ios     {:font-size      14
             :margin-top     6
             :letter-spacing -0.2}
   :android {:font-size  12
             :margin-top 2}})

(defn wnode-icon [connected?]
  {:width            40
   :height           40
   :border-radius    20
   :background-color (if connected?
                       common/color-light-blue
                       common/color-light-gray)
   :align-items      :center
   :justify-content  :center})
