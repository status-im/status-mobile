(ns status-im2.contexts.communities.discover.style
  (:require [react-native.platform :as platform]))

(def screen-title-container
  {:height           56
   :padding-vertical 12
   :justify-content  :center})

(def featured-communities-header
  {:flex-direction  :row
   :height          30
   :padding-top     8
   :margin-bottom   8
   :padding-right   20
   :justify-content :space-between})

(def featured-communities-title-container
  {:flex-direction :row
   :align-items    :center})

(def featured-list-container
  {:flex-direction :row
   :overflow       :hidden
   :margin-bottom  24})

(defn discover-communities-segments
  [fixed?]
  (merge
   {:padding-vertical 12
    :height           56
    :background-color :transparent}
   (when-not fixed?
     {:margin-top    12
      :margin-bottom 4})))

(defn discover-screen-container
  [background-color]
  {:background-color background-color
   :position         :absolute
   :top              0
   :bottom           0
   :left             0
   :right            0})

(def communities-header-container
  {:align-items     :center
   :justify-content :center})

(def render-communities-container
  {:padding-horizontal 20
   :margin-top         100})

(defn blur-tabs-header
  []
  {:padding-horizontal 20
   :position           :absolute
   :top                (if platform/ios? 100 104)
   :height             56
   :right              0
   :left               0
   :justify-content    :center
   :flex               1
   :background-color   :transparent})
