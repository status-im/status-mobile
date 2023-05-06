(ns status-im2.contexts.communities.home.style
  (:require [react-native.platform :as platform]
            [quo2.foundations.colors :as colors]))

(def tabs
  {:padding-horizontal 20
   :padding-top        16
   :padding-bottom     12})

(def blur
  {:position         :absolute
   :top              0
   :right            0
   :left             0
   :bottom           0
   :background-color :transparent})

(def empty-state-container
  {:position    :absolute
   :top         390
   :left        0
   :right       0
   :align-items :center})

(def empty-state-placeholder
  {:height           120
   :width            120
   :background-color colors/danger-50})

(defn blur-container
  [top]
  {:overflow    (if platform/ios? :visible :hidden)
   :position    :absolute
   :z-index     1
   :top         0
   :right       0
   :left        0
   :padding-top top})

