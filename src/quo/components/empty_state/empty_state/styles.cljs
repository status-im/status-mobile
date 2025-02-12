(ns quo.components.empty-state.empty-state.styles
  (:require
    [quo.foundations.colors :as colors]))

(def container
  {:padding     12
   :align-items :center})

(def image
  {:width  72
   :height 72})

(def image-container
  {:width           80
   :height          80
   :align-items     :center
   :justify-content :center})

(def text-container
  {:margin-top  12
   :align-items :center})

(defn title
  [blur?]
  (cond-> {:margin-bottom 2}
    blur? (assoc :color colors/white)))

(defn description
  [blur?]
  (cond-> {:text-align :center}
    blur? (assoc :color colors/white)))

(def button-container {:margin-top 20})

(def image-placeholder
  {:width            80
   :height           80
   :background-color colors/danger-50})
