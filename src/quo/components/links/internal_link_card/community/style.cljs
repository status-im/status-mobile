(ns quo.components.links.internal-link-card.community.style
  (:require [quo.foundations.colors :as colors]))

(defn loading-circle
  [theme]
  {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)
   :margin-right     4
   :width            16
   :height           16
   :border-radius    16})

(defn loading-stat
  [theme]
  {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)
   :margin-right     4
   :width            32
   :height           8
   :border-radius    16})

(def loading-stat-container
  {:flex-direction :row
   :align-items    :center
   :margin-right   12
   :margin-bottom  -6})

(defn loading-first-line-bar
  [theme]
  {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)
   :width            145
   :height           16
   :border-radius    6})

(defn loading-second-line-bar
  [theme]
  {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)
   :width            112
   :height           8
   :border-radius    6
   :margin-bottom    17})

(defn loading-thumbnail-box
  [theme]
  {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)
   :height           160
   :border-radius    12})

(def thumbnail
  {:width         "100%"
   :height        160
   :margin-top    6
   :border-radius 12})

(defn container
  [theme]
  {:border-width       1
   :border-color       (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :background-color   (colors/theme-colors colors/white colors/neutral-80-opa-40 theme)
   :border-radius      16
   :padding-horizontal 12
   :padding-top        10
   :padding-bottom     12
   :height             266})

(def header-container
  {:flex-direction :row
   :align-items    :center})

(def title
  {:margin-bottom 2})

(def logo
  {:width         16
   :height        16
   :border-radius 8
   :margin-right  4
   :margin-bottom 2})

(def row-spacing
  {:flex-direction :row
   :margin-bottom  12
   :margin-top     4})

(def stat-container
  {:flex-direction :row
   :margin-top     12})
