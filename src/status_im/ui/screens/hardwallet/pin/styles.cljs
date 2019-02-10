(ns status-im.ui.screens.hardwallet.pin.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def container
  {:flex             1
   :background-color colors/white})

(defstyle pin-container
  {:flex            1
   :flex-direction  :column
   :justify-content :space-between
   :padding-bottom  10
   :ios             {:margin-top 30}})

(defstyle error-container
  {:android {:margin-top 25}
   :ios     {:margin-top 28}})

(def error-text
  {:color      colors/red
   :font-size  15
   :text-align :center})

(def center-container
  {:flex-direction :column
   :align-items    :center
   :margin-top     28})

(def center-title-text
  {:font-size 22
   :color     colors/black})

(def create-pin-text
  {:font-size   15
   :padding-top 8
   :width       314
   :text-align  :center
   :color       colors/gray})

(def pin-indicator-container
  {:flex-direction  :row
   :justify-content :space-between
   :margin-top      30})

(def pin-indicator-group-container
  {:padding-horizontal 12
   :flex-direction     :row
   :justify-content    :space-between})

(defn pin-indicator [pressed?]
  {:width             16
   :height            16
   :background-color  (if pressed?
                        colors/blue
                        colors/gray-light)
   :border-radius     50
   :margin-horizontal 12})

(def waiting-indicator-container
  {:margin-top 26})

(def numpad-container
  {:margin-top 30})

(def numpad-row-container
  {:flex-direction  :row
   :justify-content :center
   :align-items     :center
   :margin-vertical 6})

(def numpad-button
  {:width             72
   :margin-horizontal 12
   :height            72
   :align-items       :center
   :justify-content   :center
   :flex-direction    :row
   :border-radius     50
   :background-color  colors/gray-background})

(def numpad-delete-button
  (assoc numpad-button :background-color colors/white
         :border-width 2
         :border-color colors/gray-background))

(def numpad-empty-button
  (assoc numpad-button :background-color colors/white
         :border-color colors/white))

(def numpad-button-text
  {:font-size 34
   :color     colors/blue})

(def numpad-empty-button-text
  {:color colors/white})
