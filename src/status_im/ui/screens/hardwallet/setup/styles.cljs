(ns status-im.ui.screens.hardwallet.setup.styles
  (:require [status-im.ui.components.colors :as colors]))

(def container
  {:flex             1
   :background-color colors/white})

(def inner-container
  {:flex-direction  :column
   :flex            1
   :align-items     :center
   :justify-content :space-between})

;; setup step

(def maintain-card-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :center
   :margin-top      81
   :width           369
   :height          60
   :border-radius   10
   :border-width    1
   :border-color    colors/blue
   :border-style    :dashed})

(def maintain-card-text
  {:padding-horizontal 20
   :font-size          12
   :color              colors/blue})

(def hardwallet-card-image-container
  {:margin-top -50})

(def hardwallet-card-image
  {:width  255
   :height 160})

(def card-is-empty-text-container
  {:margin-top 37})

(def card-is-empty-text
  {:font-size  15
   :color      colors/gray
   :text-align :center})

(def bottom-action-container
  {:background-color colors/gray-background
   :align-items      :center
   :justify-content  :center
   :flex-direction   :row
   :width            160
   :height           44
   :border-radius    10
   :margin-bottom    40})

(def begin-set-up-text
  {:font-size      14
   :color          colors/blue
   :line-height    20
   :text-transform :uppercase})

;; prepare step

(def center-container
  {:flex-direction :column
   :align-items    :center
   :height         200})

(def center-title-text
  {:font-size 22
   :color     colors/black})

(def generating-codes-for-pairing-text
  {:font-size   15
   :padding-top 8
   :width       314
   :text-align  :center
   :color       colors/gray})

(def estimated-time-text
  (assoc generating-codes-for-pairing-text :padding-top 25))

(def waiting-indicator-container
  {:height 200})

;; secret keys step

(def secret-keys-container
  {:flex-direction :column
   :align-items    :center})

(def secret-keys-title-container
  {:width 292})

(def secret-keys-title-text
  {:font-size  22
   :text-align :center
   :color      colors/black})

(def puk-code-title-text
  {:font-size   17
   :padding-top 32
   :color       colors/black})

(def puk-code-explanation-text
  {:font-size   15
   :padding-top 5
   :color       colors/gray})

(def puk-code-numbers-container
  {:width           369
   :height          64
   :margin-top      20
   :align-items     :center
   :justify-content :center
   :border-width    1
   :border-color    colors/gray-light
   :border-radius   10})

(def puk-code-text
  {:font-size  17
   :text-align :center
   :color      colors/green})

(def pair-code-title-text
  puk-code-title-text)

(def pair-code-explanation-text
  (assoc puk-code-explanation-text :text-align :center))

(def pair-code-text-container
  puk-code-numbers-container)

(def pair-code-text
  puk-code-text)

(def next-button-container
  {:flex-direction    :row
   :margin-horizontal 12
   :margin-vertical   15})
