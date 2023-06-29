(ns quo2.components.profile.collectible.style
  (:require [quo2.foundations.colors :as colors]))

(def tile-style-by-size
  {:xl {:width         160
        :height        160
        :border-radius 12}
   :lg {:width         104
        :height        104
        :border-radius 10}
   :md {:width         76
        :height        76
        :border-radius 10}
   :sm {:width         48
        :height        48
        :border-radius 8}
   :xs {:width         36
        :height        36
        :border-radius 8}})

(def tile-outer-container
  {:width   176
   :height  176
   :padding 8})

(def tile-inner-container
  {:position :relative
   :flex     1})

(def tile-sub-container
  {:position :absolute
   :width    76
   :height   76
   :bottom   0
   :right    0})

(def top-left
  {:position :absolute
   :top      0
   :left     0})

(def top-right
  {:position :absolute
   :top      0
   :right    0})

(def bottom-left
  {:position :absolute
   :bottom   0
   :left     0})

(def bottom-right
  {:position :absolute
   :bottom   0
   :right    0})

(defn remaining-tiles
  []
  (let [bg-color  (colors/theme-colors colors/neutral-20 colors/neutral-80)
        tile-size (tile-style-by-size :xs)]
    (assoc tile-size
           :justify-content  :center
           :align-items      :center
           :background-color bg-color)))

(defn remaining-tiles-text
  []
  {:color (colors/theme-colors colors/neutral-60 colors/neutral-40)})
