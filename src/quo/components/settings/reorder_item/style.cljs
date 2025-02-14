(ns quo.components.settings.reorder-item.style
  (:require
    [quo.foundations.colors :as colors]))

(defn item-container
  [blur? theme]
  {:flex-direction     :row
   :align-items        :center
   :border-radius      16
   :padding-horizontal 12
   :padding-vertical   12
   :height             48
   :background-color   (if blur?
                         colors/white-opa-5
                         (colors/theme-colors
                          colors/white
                          colors/neutral-90
                          theme))})

(def item-container-extended
  {:height 56})

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
  [theme]
  {:color  (colors/theme-colors
            colors/neutral-50
            colors/neutral-40
            theme)
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
  [theme]
  {:height 20
   :width  20
   :color  (colors/theme-colors
            colors/neutral-40
            colors/neutral-40
            theme)})

(def right-icon-container
  {:justify-content :center})

(defn placeholder-container
  [theme]
  {:background-color :transparent
   :border-width     1
   :border-color     (colors/theme-colors
                      colors/neutral-30
                      colors/neutral-80
                      theme)
   :padding          12
   :justify-content  :center
   :align-items      :center
   :border-radius    16
   :margin-bottom    24
   :border-style     :dashed})

(defn placeholder-text
  [theme]
  {:color     (colors/theme-colors
               colors/neutral-40
               colors/neutral-50
               theme)
   :font-size 13})

(defn skeleton-container
  [theme]
  {:background-color (colors/theme-colors
                      colors/neutral-5
                      colors/neutral-95
                      theme)
   :border-radius    16
   :margin-bottom    24
   :height           48})

(defn tab-container
  [theme]
  {:background-color   (colors/theme-colors
                        colors/neutral-5
                        colors/neutral-95
                        theme)
   :padding-horizontal 4
   :padding-vertical   6
   :margin-bottom      24})

(defn segmented-tab-item-container
  [theme]
  {:height            40
   :border-width      1
   :border-style      :dashed
   :margin-horizontal 2
   :border-color      (colors/theme-colors
                       colors/neutral-30
                       colors/neutral-60
                       theme)})

(defn active-segmented-tab-item-container
  [theme]
  {:height           40
   :background-color (colors/theme-colors
                      colors/neutral-30
                      colors/neutral-90
                      theme)})

(def tab-item-container
  {:flex-direction  :row
   :justify-content :center
   :align-items     :center})

(def tab-item-image
  {:height       19
   :width        19
   :margin-right 3})

(defn tab-icon
  [theme]
  {:height 16
   :width  16
   :color  (colors/theme-colors
            colors/neutral-40
            colors/neutral-40
            theme)})
