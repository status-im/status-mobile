(ns status-im.ui.screens.signing.styles
  (:require [status-im.ui.components.colors :as colors]))

(def header
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between
   :padding-top     16
   :padding-left    16
   :margin-bottom   11})

(def message-header
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between
   :padding-top     20
   :padding-left    16
   :padding-right   24
   :margin-bottom   19})

(def message
  {:background-color        colors/white
   :border-top-right-radius 16
   :border-top-left-radius  16
   :padding-bottom          40})

(def message-border
  {:margin-horizontal 24
   :max-height        96
   :min-height        35
   :flex              1
   :border-radius     8
   :border-color      colors/black-transparent
   :border-width      1
   :padding           8})

(defn sheet []
  {:background-color        colors/white
   :border-top-right-radius 16
   :border-top-left-radius  16
   :padding-bottom          40})

(defn sign-with-keycard-button [disabled?]
  {:background-color   colors/black-light
   :padding-top        2
   :border-radius      8
   :width              182
   :height             44
   :flex-direction     :row
   :justify-content    :center
   :align-items        :center
   :opacity            (if disabled? 0.1 1)
   :padding-horizontal 12})

(defn sign-with-keycard-button-text [disabled?]
  {:padding-right      2
   :padding-left       16
   :color              (if disabled? colors/black colors/white)
   :padding-horizontal 16
   :padding-vertical   10})
