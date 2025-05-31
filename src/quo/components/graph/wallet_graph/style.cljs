(ns quo.components.graph.wallet-graph.style)

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

(def empty-state
  {:width  375
   :height 116})
