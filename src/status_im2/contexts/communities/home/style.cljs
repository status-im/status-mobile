(ns status-im2.contexts.communities.home.style
  (:require [react-native.platform :as platform]))

(defn community-segments
  [padding-top]
  {:padding-bottom     12
   :padding-top        padding-top
   :padding-horizontal 20
   :background-color   :transparent})

(def communities-header-style
  {:padding-vertical 8
   :margin-top       (if platform/ios? 208 112)})

(def render-segments-container
  {:flex               1
   :padding-horizontal 20
   :padding-vertical   12})

(defn home-communities-container
  [background-color]
  {:flex             1
   :background-color background-color
   :position         :absolute
   :top              (if platform/ios? -44 0)
   :bottom           0
   :left             0
   :right            0})

(def blur-tabs-header
  {:flex             1
   :position         :absolute
   :top              (if platform/ios? 156 112)
   :height           52
   :left             0
   :right            0
   :justify-content  :center
   :background-color :transparent})
