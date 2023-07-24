(ns quo2.components.settings.category.style
  (:require [quo2.foundations.colors :as colors]))

(def container
  {:left               0
   :right              0
   :padding-horizontal 20
   :padding-top        12
   :padding-bottom     8})

(defn settings-items
  [theme blur?]
  {:margin-top       12
   :border-radius    16
   :background-color (if blur?
                       colors/white-opa-5
                       (colors/theme-colors colors/white colors/neutral-95 theme))
   :border-width     1
   :border-color     (if blur?
                       colors/white-opa-5
                       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))})

(def reorder-items
  {:margin-top 12})

(defn settings-separator
  [theme blur?]
  {:height           1
   :background-color (if blur?
                       colors/white-opa-5
                       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))})

(defn reorder-separator
  [blur? theme]
  {:height           4
   :background-color (if blur?
                       :transparent
                       (colors/theme-colors colors/neutral-5 colors/neutral-95 theme))})

(defn blur-container
  []
  {:position :absolute
   :left     0
   :right    0
   :bottom   0
   :top      0
   :overflow :hidden})

(defn blur-view
  []
  {:style       {:flex 1}
   :blur-radius 10
   :blur-type   (colors/theme-colors :light :dark)
   :blur-amount 20})
