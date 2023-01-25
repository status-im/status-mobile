(ns status-im2.contexts.communities.home.style
  (:require [react-native.platform :as platform]))

(defn community-segments 
  [padding-top]
  {:padding-bottom     12
   :padding-top        padding-top
   :height             52
   :padding-horizontal 20
   :background-color   "blue"})

(defn home-communities-container
  [background-color]
  {:flex             1
   :background-color background-color
   :position         :absolute
   :top              (if platform/ios? 0 44)
   :bottom           0
   :left             0
   :right            0})

(def blur-tabs-header
  {:position           :absolute
   :top                (if platform/ios? 60 60)
   :height             52
   :margin-top         (if platform/ios? 56 0)
   :left               0
   :right              0
   :justify-content    :center
   :flex               1})
