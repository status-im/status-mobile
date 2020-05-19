(ns status-im.ui.screens.multiaccounts.login.styles
  (:require [status-im.ui.components.colors :as colors]))

(def login-view
  {:flex              1
   :margin-horizontal 16})

(def login-badge-container
  {:margin-top 24})

(def processing-view
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def processing
  {:margin-top 16
   :color      colors/gray})

(def login-badge
  {:align-items :center})

(def login-badge-image-size 56)

(def login-badge-name
  {:margin-top  10
   :text-align  :center
   :font-weight "500"})

(def login-badge-pubkey
  {:margin-top  4
   :text-align  :center
   :color       colors/gray
   :font-family "monospace"})

(def password-container
  {:margin-top 24
   :android    {:margin-top  11
                :padding-top 13}})

(def save-password-unavailable-android
  {:margin-top 8
   :width "100%"
   :color colors/text-gray
   :text-align :center
   :flex-direction :row
   :align-items :center})

(def biometric-button
  {:justify-content  :center
   :align-items      :center
   :height           40
   :width            40
   :border-radius    20
   :margin-left      16})
