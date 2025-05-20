(ns status-im.contexts.communities.discover.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]))

(def header-height 56)

(defn container
  [theme]
  {:flex             1
   :background-color (colors/theme-colors colors/white colors/neutral-95 theme)})


(def dynamic-page-nav
  {:position    :absolute
   :top         0
   :left        0
   :right       0
   :z-index     1
   :overflow    :hidden
   :padding-top safe-area/top
   :height      (+ safe-area/top header-height)})

(defn page-nav-blur-container
  [opacity]
  [rn/stylesheet-absolute-fill
   {:opacity 0}
   {:opacity opacity}])

(defn page-nav-blur-color
  [theme]
  (colors/theme-colors colors/white-70-blur colors/neutral-95-opa-70-blur theme))

(def page-nav-blur
  {:margin-top :auto})

(def vote-button
  {:position :absolute
   :top      (+ safe-area/top 12)
   :right    20
   :z-index  1})

(def page-nav
  {:position :absolute
   :left     0
   :right    0
   :bottom   0
   :opacity  1})

(def loading-cards
  {:padding-horizontal 20
   :padding-top        4
   :row-gap            16})

(def loading-cards-gradient
  [rn/stylesheet-absolute-fill {:flex 1}])

(def fetching-message
  [{:justify-content :center
    :align-items     :center
    :row-gap         8}
   rn/stylesheet-absolute-fill])

(def fetching-top-container
  {:justify-content    :center
   :align-items        :center
   :padding-horizontal 20})

(def fetching-image
  {:width  80
   :height 80})

(def fetching-text-container
  {:padding-top    4
   :padding-bottom 12})

(def fetching-text {:text-align :center})
(def fetching-tag {:align-self :center})

(def header-info-box
  {:padding-top        8
   :padding-bottom     12
   :padding-horizontal 20})

(def community-listing-content
  {:padding-top    (+ 56 safe-area/top)
   :padding-bottom 20})

(def community-card
  {:padding-horizontal 20
   :padding-top        4
   :padding-bottom     12})
