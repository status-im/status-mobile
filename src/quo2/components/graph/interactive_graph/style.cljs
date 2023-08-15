(ns quo2.components.graph.interactive-graph.style
  (:require [quo2.foundations.typography :as typography]))

(defn x-axis-label-text
  [width y-axis-label-text-color]
  (merge
   typography/label
   {:color      y-axis-label-text-color
    :height     16
    :text-align :center
    :width      width}))

(defn y-axis-text
  [y-axis-label-text-color y-axis-label-background-color]
  (merge
   typography/label
   {:color              y-axis-label-text-color
    :padding-horizontal 3
    :margin-left        23
    :height             16
    :border-radius      6
    :overflow           :hidden
    :background-color   y-axis-label-background-color}))

(defn pointer-component
  [customization-color]
  {:width            8
   :height           8
   :border-radius    4
   :margin-left      1
   :background-color customization-color})

(defn reference-line-label
  [border-color background-color text-color]
  (merge
   typography/label
   {:align-self         :flex-end
    :right              10
    :margin-top         -9
    :padding-horizontal 5
    :padding-top        -10
    :height             19
    :line-height        14.62
    :border-radius      6
    :overflow           :hidden
    :border-color       border-color
    :border-width       2
    :background-color   background-color
    :color              text-color}))
