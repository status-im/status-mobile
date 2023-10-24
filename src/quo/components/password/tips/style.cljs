(ns quo.components.password.tips.style
  (:require
    [quo.foundations.colors :as colors]))

(def container
  {:align-self :flex-start})

(defn tip-text
  [completed?]
  {:color (if completed? colors/white-opa-40 colors/white-opa-70)})

(def strike-through
  {:position         :absolute
   :background-color colors/white
   :top              10
   :left             0
   :right            0
   :height           0.8})

