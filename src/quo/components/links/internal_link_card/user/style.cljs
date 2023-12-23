(ns quo.components.links.internal-link-card.user.style
  (:require [quo.foundations.colors :as colors]))

(defn loading-circle
  [theme]
  {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)
   :margin-right     4
   :width            16
   :height           16
   :border-radius    16})

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

(defn last-bar-line-bar
  [theme]
  {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)
   :width            271
   :height           16
   :border-radius    6})

(defn gradient-start-color
  [customization-color theme]
  (colors/theme-colors (colors/resolve-color customization-color theme 10)
                       (colors/resolve-color customization-color theme 20)
                       theme))

(defn container
  [loading? theme]
  {:border-width       1
   :border-color       (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :background-color   (colors/theme-colors colors/white colors/neutral-80-opa-40 theme)
   :border-radius      16
   :padding-horizontal 12
   :padding-top        10
   :padding-bottom     12
   :height             (if loading? 92 110)})

(def header-container
  {:flex-direction :row
   :align-items    :center})

(def title
  {:margin-bottom 2})

(def logo
  {:margin-right  6
   :width         16
   :height        16
   :border-radius 8
   :margin-bottom 2})

(def row-spacing {:flex-direction :row :margin-bottom 12})
