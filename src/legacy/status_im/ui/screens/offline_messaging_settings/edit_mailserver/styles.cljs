(ns legacy.status-im.ui.screens.offline-messaging-settings.edit-mailserver.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.utils.styles :as styles]))

(def edit-mailserver-view
  {:flex              1
   :margin-horizontal 16
   :margin-vertical   15})

(def qr-code
  {:padding 16})

(def button-container
  {:margin-top        8
   :margin-bottom     16
   :margin-horizontal 16})

(def button
  {:height          52
   :align-items     :center
   :justify-content :center
   :border-radius   8
   :ios             {:opacity 0.9}})

(styles/def connect-button
  (assoc button
         :background-color
         colors/blue))

(styles/def delete-button
  (assoc button
         :background-color
         colors/red))

(def button-label
  {:color     colors/white
   :font-size 17})
