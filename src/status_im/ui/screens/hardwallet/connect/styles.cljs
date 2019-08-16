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

(def hardwallet-card-image
  {:width  255
   :height 160})

(def turn-nfc-text-container
  {:margin-top 55})

(def status-hardwallet-text
  {:typography  :header
   :text-align  :center})

(defn bottom-action-container [nfc-enabled?]
  {:background-color (if nfc-enabled?
                       colors/blue-light
                       colors/gray-lighter)
   :width            369
   :height           80
   :border-radius    10
   :margin-bottom    20})

(def phone-nfc-on-image
  {:width  401
   :height 250})

(def phone-nfc-off-image
  {:width  301
   :height 180})

(def nfc-enabled-container
  {:flex-direction  :column
   :justify-content :space-between
   :align-items     :center
   :margin-top      50})

(def nfc-disabled-container
  {:flex-direction  :column
   :justify-content :space-between
   :align-items     :center
   :margin-top      120})

(def nfc-icon
  {:margin-left 52
   :margin-top  22})

(def go-to-settings-text
  {:text-align  :center
   :padding-top 9
   :color       colors/gray})

(def bottom-container
  {:height           52
   :justify-content  :center
   :border-top-width 1
   :border-color     colors/black-transparent})

(def product-info-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :center})

(def product-info-text
  {:text-align :center
   :color      colors/blue})

(def external-link-icon
  {:margin-left 5})
