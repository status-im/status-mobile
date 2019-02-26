(ns status-im.ui.components.checkbox.styles
  (:require [status-im.ui.components.colors :as colors]))

(def wrapper
  {:padding 16})

(defn icon-check-container [checked?]
  {:background-color (if checked? colors/blue colors/gray-lighter)
   :align-items     :center
   :justify-content :center
   :border-radius   2
   :width           18
   :height          18})

(defn icon-radio-container [checked?]
  (merge (icon-check-container checked?)
         {:border-radius 100
          :width         26
          :height        26}))

(def check-icon
  {:width  12
   :height 12})

(def plain-check-icon
  (merge check-icon
         {:tint-color colors/blue}))
