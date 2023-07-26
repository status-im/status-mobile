(ns quo2.components.settings.reorder-category.style
  (:require [quo2.foundations.colors :as colors]))

(def container
  {:left               0
   :right              0
   :padding-horizontal 20
   :padding-top        12
   :padding-bottom     8})

(defn items
  [theme blur?]
  {:margin-top 12})

(defn separator
  [theme blur?]
  {:height           1
   :background-color (if blur?
                       colors/white-opa-5
                       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))})

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
