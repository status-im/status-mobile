(ns status-im.ui.components.tooltip.styles
  (:require [status-im.ui.components.styles :as styles]))

(def tooltip-container
  {:position    :absolute
   :align-items :center
   :left        0
   :right       0
   :top         0})

(defn tooltip-animated [bottom-value opacity-value]
  {:position    :absolute
   :align-items :center
   :left        0
   :right       0
   :bottom      bottom-value
   :opacity     opacity-value})

(defn tooltip-text-container [color]
  {:padding-horizontal 16
   :padding-vertical   9
   :background-color   color
   :border-radius      8})

(defn tooltip-text [font-size]
  {:color     styles/color-red-2
   :font-size font-size})

(def tooltip-triangle
  {:width  16
   :height 8})
