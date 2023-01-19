(ns quo2.components.drawers.permission-context.style
  (:require [quo2.foundations.colors :as colors]))

(def radius 20)
(def container
  {:flex-direction          :row
   :background-color        (colors/theme-colors colors/white colors/neutral-90)
   :height                  82
   :padding-top             16
   :padding-bottom          48
   :justify-content         :center
   :padding-right           :auto
   :shadow-offset           {:width  0
                             :height 2}
   :shadow-radius           radius
   :border-top-left-radius  radius
   :border-top-right-radius radius
   :elevation               2
   :shadow-opacity          1
   :shadow-color            colors/shadow})
