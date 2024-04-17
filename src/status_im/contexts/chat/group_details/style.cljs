(ns status-im.contexts.chat.group-details.style
  (:require
    [quo.foundations.colors :as colors]
    [status-im.contexts.shell.jump-to.constants :as jump-to.constants]))

(def actions-view
  {:margin-top         8
   :margin-bottom      20
   :padding-horizontal 20})

(defn action-container
  [color]
  {:background-color (colors/theme-alpha color 0.1 0.1)
   :flex             0.29
   :border-radius    16
   :padding          12})

(defn count-container
  []
  {:width            16
   :height           16
   :border-radius    6
   :background-color (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5)})

(defn close-icon
  []
  {:background-color (colors/theme-colors colors/neutral-10 colors/neutral-80)
   :margin-left      20
   :width            32
   :height           32
   :border-radius    10
   :justify-content  :center
   :align-items      :center
   :margin-bottom    24})

(defn bottom-container
  [bottom]
  {:padding-horizontal 20
   :padding-vertical   12
   :margin-bottom      bottom
   :background-color   (colors/theme-colors colors/white colors/neutral-95-opa-70)
   :flex-direction     :row})

(def floating-shell-button
  {:position :absolute
   :bottom   jump-to.constants/default-bottom-spacing})
