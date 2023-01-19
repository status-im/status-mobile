(ns status-im2.contexts.communities.home.style
  (:require [react-native.platform :as platform]))

(def community-segments 
  {:padding-bottom     12
   :padding-top        16
   :margin-top         8
   :height             60
   :padding-horizontal 20
   :background-color   :transparent})

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
   :top                (if platform/ios? 56 60)
   :height             56
   :left               0
   :right              0
   :justify-content    :center
   :flex               1})
