(ns status-im.ui.screens.wallet.accounts.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.tabbar.styles :as tabs.styles]))

(defn card [color]
  {:width            156
   :height           145
   :margin-right     16
   :background-color color
   :shadow-offset    {:width 0 :height 2}
   :shadow-radius    8
   :shadow-opacity   1
   :shadow-color     "rgba(0, 9, 26, 0.12)"
   :elevation        2
   :border-radius    8
   :justify-content  :space-between
   :padding          12
   :padding-bottom   6
   :margin-top       5
   :margin-bottom    5})

(def add-card
  {:width            156
   :height           145
   :margin-top       5
   :margin-right     5
   :margin-bottom    5
   :background-color colors/white
   :shadow-offset    {:width 0 :height 2}
   :shadow-radius    8
   :shadow-opacity   1
   :shadow-color     "rgba(0, 9, 26, 0.12)"
   :elevation        2
   :border-radius    8
   :justify-content  :center
   :align-items      :center})

(def send-button-container
  {:position        :absolute
   :z-index         2
   :align-items     :center
   :justify-content :center
   :left            0
   :right           0
   :bottom          (+ 16 tabs.styles/tabs-diff)
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