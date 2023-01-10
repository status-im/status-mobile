(ns quo2.components.drawers.permission-context.style
  (:require [quo2.foundations.colors :as colors]))

(def radius 20)
(def container
  {:padding-top             16
   :padding-bottom          48
   :padding-horizontal      20
   :shadow-offset           {:width  0
                             :height 2}
   :shadow-radius           radius
   :border-top-left-radius  radius
   :border-top-right-radius radius
   :elevation               2
   :shadow-opacity          1
   :shadow-color            colors/shadow})
