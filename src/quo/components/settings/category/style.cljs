(ns quo.components.settings.category.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [label]
  {:left               0
   :right              0
   :padding-horizontal 20
   :padding-top        (if label 12 8)
   :padding-bottom     8})

(defn settings-items
  [{:keys [blur?]} theme]
  {:border-radius    16
   :background-color (if blur?
                       colors/white-opa-5
                       (colors/theme-colors colors/white colors/neutral-95 theme))
   :border-width     (if blur? 0 1)
   :border-color     (if blur?
                       colors/white-opa-5
                       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))})

(defn label
  [{:keys [blur?]} theme]
  {:margin-bottom 12
   :color         (if blur?
                    colors/white-opa-40
                    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))})

(def reorder-items
  {:margin-top 12})

(defn settings-separator
  [{:keys [blur?]} theme]
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
  [theme]
  {:style       {:flex 1}
   :blur-radius 10
   :blur-type   theme
   :blur-amount 20})
