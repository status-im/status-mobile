(ns status-im.ui.screens.multiaccounts.key-storage.styles
  (:require [status-im.ui.components.colors :as colors]))

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
