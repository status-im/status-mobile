(ns status-im.contexts.wallet.collectible.style
  (:require [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]))

(def container {:margin-bottom 34})

(defn- header-height
  []
  (+ 56 (safe-area/get-top)))

(defn preview-container
  []
  {:padding-horizontal 8
   :margin-top         (+ (header-height) 12)})

(def header
  {:margin-horizontal 20
   :margin-top        16
   :margin-bottom     12})

(def collection-container
  {:flex-direction :row
   :margin-top     6})

(def collection-avatar-container
  {:margin-right 8})

(def buttons-container
  {:flex-direction    :row
   :align-items       :stretch
   :margin-horizontal 20
   :margin-top        8
   :margin-bottom     20})

(def tabs
  {:margin-horizontal 20
   :margin-vertical   12})

(def send-button
  {:flex         1
   :margin-right 6})

(def opensea-button
  {:flex        1
   :margin-left 6})

(defn animated-header
  []
  {:position :absolute
   :top      0
   :left     0
   :right    0
   :height   (header-height)
   :z-index  1
   :overflow :hidden})

(defn scroll-view
  [safe-area-top]
  {:flex       1
   :margin-top (when platform/ios? (- safe-area-top))})

(def gradient-layer
  {:position    :absolute
   :top         0
   :left        0
   :right       0
   :bottom      0
   :flex        1
   :align-items :center
   :overflow    :hidden})

(def image-background
  {:height       (:height (rn/get-window))
   :aspect-ratio 1})

(def gradient
  {:position :absolute
   :top      0
   :left     0
   :right    0
   :bottom   0})

(defn background-color
  [theme]
  {:flex             1
   :background-color (colors/theme-colors colors/white colors/neutral-95 theme)})
