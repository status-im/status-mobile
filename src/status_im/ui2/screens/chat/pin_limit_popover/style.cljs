(ns status-im.ui2.screens.chat.pin-limit-popover.style
  (:require [quo2.foundations.colors :as colors]))

(defn pin-popover
  [width]
  {:width            (- width 16)
   :margin-left      8
   :background-color (colors/theme-colors colors/neutral-80-opa-90 colors/white-opa-90)
   :flex-direction   :row
   :border-radius    16
   :padding          12})

(defn pin-alert-container
  []
  {:background-color (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-40)
   :width            36
   :height           36
   :border-radius    18
   :justify-content  :center
   :align-items      :center})

(def pin-alert-circle
  {:width           18
   :height          18
   :border-radius   9
   :border-color    colors/danger-50-opa-40
   :border-width    1
   :justify-content :center
   :align-items     :center})

(def view-pinned-messages
  {:background-color   colors/primary-60
   :border-radius      8
   :justify-content    :center
   :align-items        :center
   :padding-horizontal 8
   :padding-vertical   4
   :align-self         :flex-start
   :margin-top         10})
