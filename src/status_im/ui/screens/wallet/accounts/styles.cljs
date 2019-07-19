(ns status-im.ui.screens.wallet.accounts.styles
  (:require [status-im.ui.components.colors :as colors]))

(defn card [color]
  {:width            156
   :height           145
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
   :margin-left      16
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
