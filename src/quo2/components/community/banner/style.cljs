(ns quo2.components.community.banner.style
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

(def banner-content
  {:flex           1
   :padding-top    8
   :padding-bottom 8
   :border-radius  12})

(def banner-title
  {:flex               1
   :padding-horizontal 12})

(def banner-card
  {:flex-direction    :row
   :margin-horizontal 20
   :margin-vertical   8
   :height            56
   :padding-right     12})

(def discover-illustration
  {:position :absolute
   :top      -4
   :right    0})
