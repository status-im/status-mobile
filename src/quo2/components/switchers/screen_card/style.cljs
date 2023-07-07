(ns quo2.components.switchers.screen-card.style
  (:require [quo2.foundations.colors :as colors]))

(def title
  {:margin-top        28
   :margin-horizontal 12
   :color             colors/white})

(def subtitle
  {:margin-horizontal 12
   :color             colors/neutral-40})

(def avatar-container
  {:width           48
   :height          48
   :left            12
   :top             12
   :border-radius   26
   :border-width    26
   :border-color    colors/neutral-95
   :justify-content :center
   :align-items     :center
   :position        :absolute})

(def content-container
  {:position          :absolute
   :left              0
   :right             0
   :flex-shrink       1
   :bottom            12
   :margin-horizontal 12})
