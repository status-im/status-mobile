(ns status-im2.contexts.activity-center.style
  (:require [quo2.foundations.colors :as colors]))

(def screen-padding 20)

(def header-button
  {:margin-bottom 12
   :margin-left   screen-padding})

(def header-heading
  {:padding-horizontal screen-padding
   :padding-vertical   12
   :color              colors/white})

(defn screen-container
  [window-width top bottom]
  {:flex           1
   :width          window-width
   :padding-top    (if (pos? top) (+ top 12) 12)
   :padding-bottom bottom})

(defn notification-container
  [index]
  {:margin-top         (if (zero? index) 0 4)
   :padding-horizontal 8})

(def tabs
  {:padding-left screen-padding})
