(ns quo2.components.banners.banner.style
  (:require [quo2.foundations.colors :as colors]))


(def container
  {:width              "100%"
   :height             50
   :background-color   colors/primary-50-opa-20
   :flex-direction     :row
   :align-items        :center
   :padding-horizontal 20
   :padding-vertical   10})

(def counter
  {:padding-right   22
   :height          20
   :width           20
   :justify-content :center
   :align-items     :center})