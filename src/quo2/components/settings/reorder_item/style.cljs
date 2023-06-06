(ns  quo2.components.settings.reorder-item.style 
  (:require [quo2.foundations.colors :as colors]))

(def container 
  {:padding-horizontal 20 
   :background-color (colors/theme-colors
                      colors/neutral-10 
                      colors/neutral-100)})

(def item-container 
  {:flex 1
   :flex-direction :row
   :align-items :center
   :padding 12 
   :border-radius 16
   :margin-bottom 24
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

(def placeholder-container 
  {:background-color :transparent
   :border-width 1
   :border-color (colors/theme-colors
                  colors/neutral-30
                  colors/neutral-80)
   :padding 12
   :justify-content :center
   :align-items :center
   :border-radius 16
   :margin-bottom 24
   :border-style :dashed})

(def placeholder-text 
  {:color (colors/theme-colors
           colors/neutral-40
           colors/neutral-50)
   :font-size 13})

(def skeleton-container
  {:background-color (colors/theme-colors
                      colors/neutral-5
                      colors/neutral-95)
   :border-radius 16
   :margin-bottom 24
   :height 48})

(def tab-container 
  {:background-color (colors/theme-colors
                      colors/neutral-5
                      colors/neutral-95)
   :padding 10
   :margin-bottom 24})

(def segmented-tab-item-container
  {:height 40 
   :border-width 1 
   :border-style :dashed 
   :border-color colors/neutral-30})

(def tab-item-container 
  {:flex-direction :row
   :justify-content :center
   :align-items :center})

(def tab-item-image 
  {:height 20 
   :width 20 
   :margin-right 6})

(def tab-item-label 
  {:font-size 14})
