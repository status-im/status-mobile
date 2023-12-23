(ns quo.components.links.internal-link-card.channel.style
  (:require [quo.foundations.colors :as colors]))

(defn loading-circle
  [theme]
  {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)
   :margin-right     4
   :width            16
   :height           16
   :border-radius    16})

(defn loading-first-line-bar
  [theme margin-right?]
  {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)
   :width            72
   :margin-right     (when margin-right? 4)
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
   :height           139
   :border-radius    12})

(def thumbnail
  {:width         "100%"
   :height        139
   :margin-top    8
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
   :height             215})

(def header-container
  {:flex-direction :row
   :align-items    :center})

(def title
  {:margin-bottom 2})

(def logo
  {:margin-right  6
   :width         16
   :height        16
   :border-radius 8})

(defn channel-chevron-props
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(def row-spacing
  {:flex-direction :row
   :margin-bottom  13})
