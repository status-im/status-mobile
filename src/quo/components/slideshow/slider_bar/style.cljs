(ns quo.components.slideshow.slider-bar.style
  (:require
    [quo.foundations.colors :as colors]))

(def list-wrapper
  {:align-items :center})

(defn list-bar
  [bar-width]
  {:width bar-width})

(defn item-wrapper
  [{:keys [size spacing]}]
  {:width           size
   :height          size
   :margin-left     spacing
   :flex            1
   :align-items     :center
   :justify-content :center})

(defn item
  [{:keys [size spacing active? customization-color theme blur?]}]
  {:width size
   :height size
   :border-radius spacing
   :background-color
   (cond
     (and blur? active?)       colors/white
     (and blur? (not active?)) colors/white-opa-10
     (and (not blur?) active?) (colors/resolve-color customization-color theme)
     :else                     (colors/theme-colors colors/neutral-20 colors/neutral-80 theme))})
