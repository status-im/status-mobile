(ns quo2.components.community.discover.style
  (:require [quo2.foundations.colors :as colors]))

(defn community-card
  [radius]
  {:shadow-offset    {:width  0
                      :height 2}
   :shadow-radius    radius
   :shadow-opacity   1
   :shadow-color     colors/shadow
   :elevation        1
   :border-radius    radius
   :justify-content  :space-between
   :background-color (colors/theme-colors
                      colors/white
                      colors/neutral-90)})


(def discover-illustration
  {:position :absolute
   :top      -4
   :right    0})