(ns quo2.components.empty-state.empty-state.styles
  (:require [quo2.foundations.colors :as colors]))

(def container
  {:padding     12
   :align-items :center})

(def image
  {:width  80
   :height 80})

(def text-container
  {:margin-top  12
   :align-items :center})

(defn title
  [blur?]
  (cond-> {:margin-bottom 2}
    blur? (assoc :color colors/white)))

(defn description
  [blur?]
  (when blur?
    {:color colors/white}))

(def button-container {:margin-top 20})

(def image-placeholder
  {:width            80
   :height           80
   :background-color colors/danger})
