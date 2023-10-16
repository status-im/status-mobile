(ns quo2.components.switchers.base-card.style
  (:require
    [quo2.foundations.colors :as colors]))

(defn base-container
  [customization-color]
  {:width            160
   :height           160
   :border-radius    16
   :background-color (colors/custom-color customization-color 50 40)})

(def thumb-card
  {:width            160
   :height           120
   :border-radius    16
   :bottom           0
   :position         :absolute
   :background-color colors/neutral-95
   :padding          12})

(def close-button
  {:position :absolute
   :right    8
   :top      8})
