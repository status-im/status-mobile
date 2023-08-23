(ns quo2.components.drawers.action-drawers.style
  (:require [quo2.foundations.colors :as colors]))

(defn divider
  [theme]
  {:border-top-width 1
   :border-top-color (colors/theme-colors colors/neutral-10 colors/neutral-90 theme)
   :margin-top       8
   :margin-bottom    7
   :align-items      :center
   :flex-direction   :row})

(defn container
  [sub-label disabled?]
  {:border-radius     12
   :height            (if sub-label 58 50)
   :opacity           (when disabled? 0.3)
   :margin-horizontal 8})

(defn row-container
  [sub-label]
  {:height            (if sub-label 58 50)
   :margin-horizontal 12
   :flex-direction    :row})

(defn left-icon
  [sub-label?]
  {:height        20
   :margin-top    (if sub-label? 10 :auto)
   :margin-bottom (when-not sub-label? :auto)
   :margin-right  12
   :width         20})

(def text-container
  {:flex            1
   :justify-content :center})

(def right-side-container
  {:flex-direction :row
   :align-items    :center})

(def right-icon
  {:height        20
   :margin-top    :auto
   :margin-bottom :auto
   :width         20})

(defn right-text
  [theme]
  {:color        (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :margin-right 12})
