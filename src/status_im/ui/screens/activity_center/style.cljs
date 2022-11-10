(ns status-im.ui.screens.activity-center.style
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
   :padding-top    (if (> top 0) (+ top 12) 12)
   :padding-bottom bottom})

(def notifications-container
  {:flex-grow 1})

(defn notification-container
  [index]
  {:margin-top         (if (= 0 index) 0 4)
   :padding-horizontal screen-padding})

(def tabs
  {:padding-left screen-padding})
