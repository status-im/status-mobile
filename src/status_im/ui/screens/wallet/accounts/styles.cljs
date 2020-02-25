(ns status-im.ui.screens.wallet.accounts.styles
  (:require [status-im.ui.components.colors :as colors]))

(def card-common
  {:margin-vertical   16
   :margin-horizontal 8
   :width             156
   :height            145
   :shadow-offset     {:width 0 :height 2}
   :shadow-radius     8
   :shadow-opacity    1
   :shadow-color      "rgba(0, 9, 26, 0.12)"
   :elevation         3
   :border-radius     8})

(defn card [color]
  (merge card-common
         {:background-color   color
          :justify-content    :space-between
          :padding-horizontal 12
          :padding-top        12
          :padding-bottom     6}))

(def add-card
  (merge card-common
         {:background-color colors/white
          :justify-content  :center
          :align-items      :center}))

(def send-button-container
  {:position        :absolute
   :z-index         2
   :align-items     :center
   :justify-content :center
   :left            0
   :right           0
   :bottom          16
   :height          40})

(def send-button
  {:width            40
   :height           40
   :background-color colors/blue
   :border-radius    20
   :align-items      :center
   :justify-content  :center
   :shadow-offset    {:width 0 :height 1}
   :shadow-radius    6
   :shadow-opacity   1
   :shadow-color     "rgba(0, 12, 63, 0.2)"
   :elevation        2})
