(ns status-im2.contexts.communities.home.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]))

(def header-height 245)

(def tabs
  {:padding-horizontal 20
   :padding-top        8
   :padding-bottom     12})

(def blur
  {:position :absolute
   :top      0
   :right    0
   :left     0
   :bottom   0})

(defn empty-state-container
  [top]
  {:margin-top      (+ header-height top)
   :margin-bottom   44
   :flex            1
   :justify-content :center})

(def empty-state-placeholder
  {:height           120
   :width            120
   :background-color colors/danger-50})

(defn header-spacing
  [top]
  {:height (+ header-height top)})

(defn blur-container
  [top]
  {:overflow    (if platform/ios? :visible :hidden)
   :position    :absolute
   :z-index     1
   :top         0
   :right       0
   :left        0
   :padding-top top})

(def card-bottom-override {:margin-bottom 16}) ; Original 8 + 8 from tabs top padding

(defn animated-card-container
  [height opacity]
  (reanimated/apply-animations-to-style {:height height :opacity opacity}
                                        {:overflow :hidden}))

(defn animated-card-translation
  [translate-y]
  (reanimated/apply-animations-to-style {:transform [{:translate-y translate-y}]}
                                        {}))
