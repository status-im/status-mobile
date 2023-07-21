(ns quo2.components.settings.reorder-item.style
  (:require [quo2.foundations.colors :as colors]))

(defn item-container
  [blur?]
  {:flex-direction     :row
   :align-items        :center
   :border-radius      16
   :padding-horizontal 12
   :padding-vertical   12
   :background-color   (if blur?
                         colors/white-opa-5
                         (colors/theme-colors
                          colors/white
                          colors/neutral-90))})

(def item-container-extended
  {:height 52})

(def body-container
  {:flex           1
   :margin-left    12
   :flex-direction :row
   :align-items    :center
   :margin-right   -6})

(def image-container
  {:margin-right 8})

(defn image
  [size]
  {:width  size
   :height size})

(defn chevron
  []
  {:color  (colors/theme-colors
            colors/neutral-50
            colors/neutral-40)
   :height 14
   :width  14})

(def item-subtitle
  {:color colors/neutral-50})

(def right-text
  {:font-size 15
   :color     colors/neutral-40})

(def text-container
  {:flex            1
   :flex-direction  :row
   :justify-content :space-between
   :margin-right    8})

(defn right-icon
  []
  {:height 20
   :width  20
   :color  (colors/theme-colors
            colors/neutral-40
            colors/neutral-40)})

(def right-icon-container
  {:justify-content :center})

(defn placeholder-container
  []
  {:background-color :transparent
   :border-width     1
   :border-color     (colors/theme-colors
                      colors/neutral-30
                      colors/neutral-80)
   :padding          12
   :justify-content  :center
   :align-items      :center
   :border-radius    16
   :margin-bottom    24
   :border-style     :dashed})

(defn placeholder-text
  []
  {:color     (colors/theme-colors
               colors/neutral-40
               colors/neutral-50)
   :font-size 13})

(defn skeleton-container
  []
  {:background-color (colors/theme-colors
                      colors/neutral-5
                      colors/neutral-95)
   :border-radius    16
   :margin-bottom    24
   :height           48})

(defn tab-container
  []
  {:background-color   (colors/theme-colors
                        colors/neutral-5
                        colors/neutral-95)
   :padding-horizontal 4
   :padding-vertical   6
   :margin-bottom      24})

(defn segmented-tab-item-container
  []
  {:height            40
   :border-width      1
   :border-style      :dashed
   :margin-horizontal 2
   :border-color      (colors/theme-colors
                       colors/neutral-30
                       colors/neutral-60)})

(defn active-segmented-tab-item-container
  []
  {:height           40
   :background-color (colors/theme-colors
                      colors/neutral-30
                      colors/neutral-90)})

(def tab-item-container
  {:flex-direction  :row
   :justify-content :center
   :align-items     :center})

(def tab-item-image
  {:height       19
   :width        19
   :margin-right 3})

(def tab-item-label
  {:font-size 14})

(defn tab-icon
  []
  {:height 16
   :width  16
   :color  (colors/theme-colors
            colors/neutral-40
            colors/neutral-40)})
