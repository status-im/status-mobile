(ns quo2.components.drawers.drawer-top.style 
  (:require [quo2.foundations.colors :as colors]))

(def container
  {:padding-horizontal 20
   :padding-bottom 12
   :flex-direction :row
   :background-color "#f3f3f3"})

(def body-container
  {:flex 1})

(def title
  {:flex 1})

(defn description
  [theme blur?]
  {:color (if blur? 
            colors/white-opa-40 
            (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))})

(def left-container
  {:margin-right 8
   :justify-content :center})
