(ns status-im.ui.components.checkbox.styles
  (:require [status-im.ui.components.colors :as colors]))

(def wrapper
  {:padding 16})

(defn icon-check-container [checked?]
  {:background-color (if checked? colors/blue colors/gray-lighter)
   :align-items     :center
   :justify-content :center
   :border-radius  2
   :width          24
   :height         24})

(def check-icon
  {:width  12
   :height 12})
