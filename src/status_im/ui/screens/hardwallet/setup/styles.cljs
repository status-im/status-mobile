(ns status-im.ui.screens.hardwallet.setup.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
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
   :justify-content :space-between
   :margin-top      81
   :width           "90%"
   :height          60
   :border-radius   10
   :border-width    1
   :border-color    colors/blue
   :border-style    :dashed})

(def maintain-card-text
  {:padding-horizontal 20
   :font-size          12
   :width              232
   :color              colors/blue})

(def hardwallet-icon-container
  {:margin-left    20
   :flex-direction :row
   :align-items    :center})

(defn hardwallet-icon-indicator-small-container [opacity]
  {:margin-left 4
   :opacity     opacity})

(defn hardwallet-icon-indicator-middle-container [opacity]
  {:margin-left 1
   :opacity     opacity})

(defn hardwallet-icon-indicator-big-container [opacity]
  {:opacity opacity})

(def hardwallet-card-image-container
  {:margin-top  81
   :flex        1
   :align-items :center})

(def hardwallet-card-image
  {:width  255
   :height 160})

(def loading-view-container
  {:flex           1
   :flex-direction :column
   :align-items    :center
   :margin-top     100})

(def card-with-button-view-container
  {:flex           1
   :flex-direction :column
   :align-items    :center})

(def enter-pair-code-container
  {:flex            1
   :flex-direction  :column
   :justify-content :space-between
   :margin-top      80})

(def center-text-container
  {:margin-top 37})

(def center-text
  {:font-size  15
   :color      colors/gray
   :text-align :center})

(def bottom-button-container
  {:background-color colors/gray-background
   :align-items      :center
   :justify-content  :center
   :flex-direction   :row
   :width            160
   :height           44
   :border-radius    10
   :margin-bottom    40})

(def bottom-button-text
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
  {:flex            1
   :flex-direction  :column
   :justify-content :space-between
   :margin-top      40})

(def secret-keys-inner-container
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
   :width       "90%"
   :text-align  :center
   :padding-top 5
   :color       colors/gray})

(def puk-code-numbers-container
  {:justify-content :center
   :flex-direction  :row})

(defstyle puk-code-numbers-inner-container
  {:width            "85%"
   :android          {:margin-horizontal 16}
   :height           64
   :margin-top       20
   :align-items      :center
   :justify-content  :center
   :border-width     1
   :border-color     colors/gray-light
   :border-radius    10})

(def puk-code-text
  {:font-size  17
   :text-align :center
   :color      colors/green})

(def next-button-container
  {:flex-direction  :row
   :margin-vertical 15})

(def secret-keys-next-button-container
  (assoc next-button-container
         :width "100%"
         :margin-right 12))

;; enter pair code

(def enter-pair-code-title-container
  {:flex-direction :column
   :align-items    :center})

(defn enter-pair-code-input-container [width]
  {:width      (* width 0.9)
   :margin-top 10})

(def enter-pair-code-title-text
  {:font-size  22
   :text-align :center
   :color      colors/black})

(def enter-pair-code-explanation-text
  {:font-size   15
   :padding-top 5
   :color       colors/gray})
