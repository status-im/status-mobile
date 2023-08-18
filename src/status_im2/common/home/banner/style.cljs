(ns status-im2.common.home.banner.style
  (:require [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]))

(def ^:private card-height (+ 56 16))
(def ^:private max-scroll (+ card-height 8))

(def fill-space
  {:position :absolute
   :top      0
   :right    0
   :left     0
   :bottom   0})

(defn- animated-card-translation-y
  [scroll-shared-value]
  (reanimated/interpolate scroll-shared-value [0 max-scroll] [0 (- max-scroll)] :clamp))

(defn- animated-card-translation-y-reverse
  [scroll-shared-value]
  (reanimated/interpolate scroll-shared-value [0 max-scroll] [0 (+ max-scroll)] :clamp))

(defn banner-card-blur-layer
  [scroll-shared-value]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-y (animated-card-translation-y scroll-shared-value)}]}
   {:overflow (if platform/ios? :visible :hidden)
    :z-index  1
    :position :absolute
    :top      0
    :right    0
    :left     0
    :height   (+ (safe-area/get-top) 244)}))

(defn banner-card-hiding-layer
  [scroll-shared-value]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-y (animated-card-translation-y-reverse scroll-shared-value)}]}
   {:z-index     2
    :position    :absolute
    :top         0
    :right       0
    :left        0
    :padding-top (safe-area/get-top)}))

(defn animated-banner-card
  [scroll-shared-value]
  (reanimated/apply-animations-to-style
   {:opacity   (reanimated/interpolate scroll-shared-value [0 card-height] [1 0] :clamp)
    :transform [{:translate-y (animated-card-translation-y scroll-shared-value)}]}
   {}))

(defn banner-card-tabs-layer
  [scroll-shared-value]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-y (animated-card-translation-y scroll-shared-value)}]}
   {:z-index  3
    :position :absolute
    :top      (+ (safe-area/get-top) 192)
    :right    0
    :left     0}))

(def banner-card-tabs
  {:padding-horizontal 20
   :padding-top        8
   :padding-bottom     12})
