(ns status-im.ui.screens.offline-messaging-settings.edit-mailserver.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.components.colors :as colors]))

(def edit-mailserver-view
  {:flex              1
   :margin-horizontal 16
   :margin-vertical   15})

(def input-container
  {:flex-direction    :row
   :align-items       :center
   :justify-content   :space-between
   :border-radius     styles/border-radius
   :height            52
   :margin-top        15})

(defstyle input
  {:flex               1
   :font-size          15
   :letter-spacing     -0.2
   :android            {:padding 0}})

(def qr-code
  {:margin-right 14})

(def bottom-container
  {:flex-direction    :row
   :margin-horizontal 12
   :margin-vertical   15})

(def button-container
  {:margin-top        8
   :margin-bottom     16
   :margin-horizontal 16})

(def button
  {:height           52
   :align-items      :center
   :justify-content  :center
   :border-radius    8
   :ios              {:opacity 0.9}})

(defstyle connect-button
  (assoc button
         :background-color colors/blue))

(defstyle delete-button
  (assoc button
         :background-color colors/red))

(defstyle button-label
  {:color   colors/white
   :ios     {:font-size      17
             :letter-spacing -0.2}
   :android {:font-size 14}})
