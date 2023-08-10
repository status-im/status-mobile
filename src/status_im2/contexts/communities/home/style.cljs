(ns status-im2.contexts.communities.home.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]))

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

(def empty-state-placeholder
  {:height           120
   :width            120
   :background-color colors/danger-50})

(defn blur-banner-layer
  [animated-translation-y]
  (let [fixed-height (+ (safe-area/get-top) 244)]
    (reanimated/apply-animations-to-style
     {:transform [{:translate-y animated-translation-y}]}
     {:overflow (if platform/ios? :visible :hidden)
      :z-index  1
      :position :absolute
      :top      0
      :right    0
      :left     0
      :height   fixed-height})))

(defn hiding-banner-layer
  []
  {:z-index     2
   :position    :absolute
   :top         0
   :right       0
   :left        0
   :padding-top (safe-area/get-top)})

(defn tabs-banner-layer
  [animated-translation-y]
  (let [top-offset (+ (safe-area/get-top) 192)]
    (reanimated/apply-animations-to-style
     {:transform [{:translate-y animated-translation-y}]}
     {:z-index  3
      :position :absolute
      :top      top-offset
      :right    0
      :left     0})))

(def animated-card-container {:overflow :hidden})

(defn animated-card
  [opacity translate-y]
  (reanimated/apply-animations-to-style {:opacity   opacity
                                         :transform [{:translate-y translate-y}]}
                                        {}))
