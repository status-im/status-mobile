(ns status-im.chat.commands.impl.transactions.styles
  (:require [status-im.ui.components.colors :as colors]))

(def asset-container
  {:flex-direction   :row
   :align-items      :center
   :justify-content  :space-between
   :padding-vertical 11})

(def asset-main
  {:flex            1
   :flex-direction  :row
   :align-items     :center})

(def asset-icon
  {:width        30
   :height       30
   :margin-left  14
   :margin-right 12})

(def asset-symbol
  {:color colors/black})

(def asset-name
  {:color        colors/gray
   :padding-left 4})

(def asset-balance
  {:color         colors/gray
   :padding-right 14})

(def asset-separator
  {:height           1
   :background-color colors/gray-light
   :margin-left      56})
