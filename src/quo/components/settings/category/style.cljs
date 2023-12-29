(ns quo.components.settings.category.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [label]
  #js {:left               0
       :right              0
       :paddingHorizontal 20
       :paddingTop        (when label 12)
       :paddingBottom     8})

(defn settings-items
  [{:keys [blur? theme]}]
  #js {:marginTop       12
       :borderRadius    16
       :backgroundColor (if blur?
                          colors/white-opa-5
                          (colors/theme-colors colors/white colors/neutral-95 theme))
       :borderWidth     (if blur? 0 1)
       :borderColor     (if blur?
                          colors/white-opa-5
                          (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))})

(defn label
  [{:keys [blur? theme]}]
  #js {:color (if blur?
                colors/white-opa-40
                (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))})

(def reorder-items
  {:margin-top 12})

(defn settings-separator
  [{:keys [blur? theme]}]
  #js {:height           1
       :backgroundColor (if blur?
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
