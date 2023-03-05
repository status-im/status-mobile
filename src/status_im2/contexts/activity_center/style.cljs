(ns status-im2.contexts.activity-center.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(def screen-padding 20)

(def header-container
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-horizontal screen-padding
   :margin-bottom      12})

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

(def tabs-container
  {:flex       1
   :align-self :stretch})

(def filter-toggle-container
  {:flex-grow     0
   :margin-left   16
   :padding-right screen-padding})

(def tabs-and-filter-container
  {:flex-direction   :row
   :padding-vertical 12})

(def empty-container
  {:align-items      :center
   :flex             1
   :justify-content  :center
   :padding-vertical 12})

(def empty-title
  {:padding-bottom 2
   :color          colors/white})

(def empty-subtitle
  {:color colors/white})

(def empty-rectangle-placeholder
  {:width            120
   :height           120
   :background-color colors/danger-50
   :margin-bottom    20})

(def blur-background
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(defn options-content-container
  [bottom-inset]
  {:background-color        colors/neutral-100
   :padding-bottom          (if platform/ios? bottom-inset 20)
   :padding-top             20
   :border-top-left-radius  20
   :border-top-right-radius 20})

(def options-modal-container
  {:justify-content :flex-end
   :margin          0
   :width           "100%"
   :align-self      :center})
