(ns quo2.components.loaders.community-card-skeleton.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.shadows :as shadows]))

(defn card
  [width theme]
  (merge
   {:width            width
    :height           230
    :border-radius    16
    :background-color (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)}
   (shadows/get 2)))

(defn avatar
  [theme]
  {:width            48
   :height           48
   :border-radius    24
   :position         :absolute
   :top              -24
   :left             12
   :background-color (colors/theme-colors colors/neutral-20 colors/neutral-70 theme)
   :border-color     (colors/theme-colors colors/white colors/neutral-90 theme)
   :border-width     2})

(defn lock
  [theme]
  {:width            48
   :height           24
   :border-radius    20
   :position         :absolute
   :top              8
   :right            8
   :background-color (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})


(defn cover-container
  [theme]
  {:flex-direction          :row
   :height                  64
   :border-top-right-radius 16
   :border-top-left-radius  16
   :background-color        (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})

(defn content-container
  [theme]
  {:position           :absolute
   :heigth             190
   :top                40
   :bottom             0
   :left               0
   :right              0
   :padding-horizontal 12
   :border-radius      16
   :background-color   (colors/theme-colors colors/white colors/neutral-90 theme)})

(def tags-container
  {:margin-top 20 :flex-direction :row :align-items :center})

(def stats-container
  {:margin-top 20 :flex-direction :row :align-items :center})

(defn stat-circle
  [theme margin-left]
  {:height           14
   :width            14
   :border-radius    7
   :background-color (colors/theme-colors colors/neutral-20 colors/neutral-70 theme)
   :margin-left      margin-left})

(defn stat-line
  [theme margin-left]
  {:height           12
   :width            50
   :border-radius    5
   :background-color (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :margin-left      margin-left})

(defn tag
  [theme margin-left]
  {:height           24
   :width            76
   :border-radius    20
   :background-color (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)
   :margin-left      margin-left})

(defn content-line
  [theme width margin-top]
  {:width            width
   :height           16
   :background-color (colors/theme-colors colors/neutral-20 colors/neutral-70 theme)
   :border-radius    5
   :margin-top       margin-top})

(def card-content-container {:margin-top 36})
