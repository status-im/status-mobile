(ns quo.components.graph.wallet-graph.style
  (:require
    [quo.foundations.colors :as colors]))

(def gradient-background
  {:height   294
   :position :absolute
   :left     0
   :right    0
   :bottom   0})

(def x-axis-label-text-style ; We need this to remove unnecessary bottom spacing from graph
  {:margin-bottom -3
   :padding-top   -10
   :height        0})

(def illustration
  {:height           96
   :background-color colors/danger-50
   :align-items      :center
   :justify-content  :center})

(def empty-state
  {:width  375
   :height 116})
