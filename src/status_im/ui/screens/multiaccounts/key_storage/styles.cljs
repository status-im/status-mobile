(ns status-im.ui.screens.multiaccounts.key-storage.styles
  (:require [quo.design-system.colors :as colors]))

(def help-text-container
  {:width            "60%"
   :align-self       :center
   :padding-vertical 24})

(def help-text
  {:text-align :center})

(def popover-title
  {:typography    :title-bold
   :margin-top    8
   :margin-bottom 24})

(def popover-body-container
  {:flex-wrap       :wrap
   :flex-direction  :row
   :justify-content :center
   :text-align      :center})

(def popover-text
  {:color       colors/gray
   :text-align  :center
   :line-height 22})

(def header
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between
   :padding-top     16
   :padding-left    16
   :margin-bottom   11})