(ns quo.components.overlay.style
  (:require [quo.foundations.colors :as colors]))

(defn overlay-background
  [type]
  (let [background-color (case type
                           :shell  colors/overlay-background-blur
                           :drawer colors/neutral-100-opa-70-blur
                           nil)]
    {:position         :absolute
     :top              0
     :left             0
     :right            0
     :bottom           0
     :background-color background-color}))

(def container
  {:flex 1})

(def blur-container
  {:flex             1
   :background-color :transparent})
