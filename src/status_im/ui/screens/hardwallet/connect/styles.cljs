(ns status-im.ui.screens.hardwallet.connect.styles
  (:require [status-im.ui.components.colors :as colors]))

(def container
  {:flex             1
   :background-color colors/white})

(def hardwallet-connect
  {:flex-direction  :column
   :flex            1
   :align-items     :center
   :justify-content :space-between})

(def hardwallet-card-image-container
  {:margin-top 120})

(def hardwallet-card-image
  {:width  255
   :height 160})

(def hardwallet-card-image-small
  {:width        44
   :height       28
   :margin-right 20})

(def status-hardwallet-text-container
  {:margin-top 30})

(def status-hardwallet-text
  {:font-size   22
   :font-weight :bold
   :color       colors/black
   :text-align  :center})

(def link-card-text
  {:text-align         :center
   :font-size          15
   :color              colors/gray
   :padding-horizontal 80
   :padding-vertical   10})

(def bottom-action-container
  {:background-color colors/gray-lighter
   :width            369
   :height           80
   :border-radius    10
   :margin-bottom    20})

(def nfc-enabled-container
  {:flex-direction  :row
   :justify-content :space-between
   :align-items     :center})

(def phone-nfc-image
  {:width       54
   :height      72
   :margin-left 20
   :margin-top  8
   :align-items :baseline})

(def hold-card-text
  {:width          186
   :text-align     :center
   :font-size      14
   :color          colors/blue
   :line-height    20
   :text-transform :uppercase
   :margin-right   40})

(def nfc-disabled-container
  {:flex-direction  :row
   :justify-content :space-between
   :align-items     :center})

(def nfc-icon
  {:margin-left 52
   :margin-top  22})

(def nfc-disabled-actions-container
  {:flex-direction  :column
   :align-items     :center
   :justify-content :space-between
   :margin-right    100
   :margin-top      20})

(def turn-nfc-text
  {:text-transform :uppercase
   :line-height    20
   :letter-spacing 0.5
   :color          colors/gray})

(def go-to-settings-text
  {:color colors/gray})

