(ns status-im2.contexts.chat.composer.gradients.style
  (:require
    [quo2.foundations.colors :as colors]
    [quo2.foundations.typography :as typography]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]))

(defn top-gradient-style
  [opacity z-index]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:height   80
    :position :absolute
    :z-index  z-index
    :top      0
    :left     0
    :right    0}))

(defn top-gradient
  [opacity z-index]
  {:colors [(colors/theme-colors colors/white-opa-0 colors/neutral-95-opa-0)
            (colors/theme-colors colors/white colors/neutral-95)]
   :start  {:x 0 :y 1}
   :end    {:x 0 :y 0}
   :style  (top-gradient-style opacity z-index)})

(def bottom-gradient-style
  {:height   (if platform/ios? (:line-height typography/paragraph-1) 32)
   :position :absolute
   :bottom   0
   :left     0
   :right    0
   :z-index  2})

(defn bottom-gradient
  []
  {:colors [(colors/theme-colors colors/white colors/neutral-95)
            (colors/theme-colors colors/white-opa-0 colors/neutral-95-opa-0)]
   :start  {:x 0 :y 1}
   :end    {:x 0 :y 0}
   :style  bottom-gradient-style})

