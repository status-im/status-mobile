(ns status-im.ui2.screens.chat.group-details.style
  (:require [quo2.foundations.colors :as colors]))

(defn actions-view []
  {:flex-direction     :row
   :justify-content    :space-between
   :margin-vertical    20
   :padding-horizontal 20})

(defn action-container [color]
  {:background-color (colors/theme-alpha color 0.1 0.1)
   :flex             0.29
   :border-radius    16
   :padding          12})

(defn count-container []
  {:width            16
   :height           16
   :border-radius    6
   :background-color (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5)})
