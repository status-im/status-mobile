(ns quo2.components.drawers.drawer-buttons.style
  (:require [quo2.foundations.colors :as colors]))

(def outer-container {:height 216})

(def top-card
  {:height             "100%"
   :display            :flex
   :padding-vertical   12
   :padding-horizontal 20
   :border-radius      20
   :background-color   colors/neutral-80})

(def bottom-card
  {:position           :absolute
   :top                80
   :left               0
   :right              0
   :bottom             0
   :display            :flex
   :padding-vertical   12
   :padding-horizontal 20
   :border-radius      20
   :background-color   (colors/alpha colors/white 0.05)})
