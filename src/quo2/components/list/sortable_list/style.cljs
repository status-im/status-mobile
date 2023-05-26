(ns quo2.components.list.sortable-list.style 
  (:require [quo2.foundations.colors :as colors]))

(def container 
  {:padding 20 
   :background-color (colors/theme-colors
                      colors/neutral-20 
                      colors/neutral-100)})

(def item-container 
  {:flex 1
   :flex-direction :row
   :align-items :center
   :padding 12 
   :border-radius 16
   :margin-bottom 24
   :z-index 20
   :background-color (colors/theme-colors 
                      colors/custom-color :white 
                      colors/neutral-90)})

(def left-icon 
  {:color colors/neutral-50})

(def body-container 
  {:flex 1
   :margin-left 12
   :flex-direction :row
   :align-items :center})

(def image-container {:margin-right 12})

(defn image 
  [size]
  {:width  size
   :height size})

(def item-text 
  {:font-size 15})

(def chevron 
  {:color (colors/theme-colors
           colors/neutral-50
           colors/neutral-40)
   :height 14
   :width 14})

(def item-subtitle 
  {:color colors/neutral-50
   :font-size 13})

(def right-text 
  {:font-size 15
   :color colors/neutral-50})

(def text-container
  {:flex 1
   :flex-direction :row
   :justify-content "space-between"
   :margin-right 6})

(def right-icon 
  {:height 20
   :width 20
   :color (colors/theme-colors
           colors/neutral-50 
           colors/neutral-40)})

(def right-icon-container
  {:justify-content :center})
