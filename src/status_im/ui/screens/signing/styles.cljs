(ns status-im.ui.screens.signing.styles
  (:require [status-im.ui.components.colors :as colors]))

(def header
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between
   :padding-top     16
   :padding-left    16
   :padding-right   24
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
  {:background-color        :white
   :flex-grow               1
   :flex-shrink             1
   :flex-basis              600
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

(def sheet
  {:background-color        :white
   :border-top-right-radius 16
   :border-top-left-radius  16
   :padding-bottom          40})