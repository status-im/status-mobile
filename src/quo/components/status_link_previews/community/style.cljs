(ns quo.components.status-link-previews.community.style
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

(def loading-stat-container {:flex-direction :row :align-items :center :margin-right 12})

(defn loading-first-line-bar
  [theme]
  {:style {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)
           :width            145
           :height           16
           :border-radius    6}})

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
   :margin-right     4
   :width            271
   :height           139
   :border-radius    16})

(def thumbnail
  {:width         "100%"
   :height        139
   :margin-top    8
   :border-radius 12})

(def row-spacing {:flex-direction :row :margin-bottom 12})
