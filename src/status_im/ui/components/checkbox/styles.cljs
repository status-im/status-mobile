(ns status-im.ui.components.checkbox.styles
  (:require [status-im.ui.components.colors :as colors]))

(def wrapper
  {:width 24 :height 24 :align-items :center :justify-content :center})

(defn icon-check-container [checked?]
  {:background-color (if checked? colors/blue colors/gray-lighter)
   :align-items     :center
   :justify-content :center
   :border-radius   2
   :width           18
   :height          18})

(def check-icon
  {:width  16
   :height 16})
