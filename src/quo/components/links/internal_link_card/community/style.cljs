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
  [theme size]
  {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)
   :height           (if (= :message size) 139 160)
   :border-radius    12})

(defn thumbnail
  [size]
  {:width         "100%"
   :height        (if (= :message size) 139 160)
   :margin-top    6
   :border-radius 12})

(defn container
  [size theme]
  {:border-width       1
   :border-color       (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :background-color   (colors/theme-colors colors/white colors/neutral-80-opa-40 theme)
   :border-radius      16
   :padding-horizontal 12
   :padding-top        10
   :padding-bottom     12
   :width              (if (= :message size) 295 335)
   :flex               1})

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
