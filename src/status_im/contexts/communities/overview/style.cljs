(ns status-im.contexts.communities.overview.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]))

(defn fetching-placeholder
  [top-inset]
  {:flex       1
   :margin-top top-inset})

(def community-overview-container
  {:flex 1})

(defn header-cover-image
  [background-color]
  {:width            "100%"
   :background-color background-color
   :height           (+ 20 ;; Area hidden by sheet on top but visible with rounded borders
                        92
                        safe-area/top)})

(def cover-image {:flex 1})

(defn cover-image-blur-container
  [header-opacity]
  [rn/stylesheet-absolute-fill
   {:opacity 0}
   {:opacity header-opacity}])

(defn cover-image-blur-layer
  [theme]
  [rn/stylesheet-absolute-fill
   {:background-color (colors/theme-colors colors/white-70-blur
                                           colors/neutral-95-opa-70-blur
                                           theme)}])

(def ^:private page-nav-container-base-style
  {:height (+ 12 ;; padding-top
              12 ;; padding-bottom
              32) ;; button size
   :width  "100%"})

(defn page-nav-container
  [opposite-header-opacity]
  [rn/stylesheet-absolute-fill
   page-nav-container-base-style
   {:top     (- safe-area/top 12) ;; -12 to place the button next to the safe-area
    :opacity 1}
   {:opacity opposite-header-opacity}])

(defn page-nav-container-blur
  [header-opacity]
  [rn/stylesheet-absolute-fill
   page-nav-container-base-style
   {:top     (- safe-area/top 12) ;; -12 to place the button next to the safe-area
    :opacity 0}
   {:opacity header-opacity}])

(def community-logo
  {:position         :absolute
   :z-index          1
   :width            88
   :height           88
   :border-radius    44
   :justify-content  :center
   :align-items      :center
   :background-color :white
   :transform        [{:translate-x 20} {:scale 1}]
   :transform-origin "bottom left"})

(defn community-logo-bg-color
  [theme]
  {:background-color (colors/theme-colors colors/white
                                          colors/neutral-95
                                          theme)})

(def community-logo-image
  {:width         80
   :height        80
   :border-radius 40})

(defn community-info
  [theme]
  {:border-top-left-radius  20
   :border-top-right-radius 20
   :padding-top             44
   :background-color        (colors/theme-colors colors/white colors/neutral-95 theme)})

(def status-tag-position
  {:position :absolute :top 12 :right 12 :opacity 0})

(def community-name-and-description
  {:padding-vertical   12
   :padding-horizontal 20
   :row-gap            8})

(def community-stats
  {:flex-direction     :row
   :column-gap         12
   :height             22
   :padding-horizontal 20
   :opacity            0})

(def community-tags
  {:margin-top         16
   :padding-horizontal 20
   :padding-bottom     20})

(def community-tags-last-item
  {:margin-right (* 2 20)})

(defn request-to-join-button
  [tags?]
  {:padding-left   20
   :padding-right  20
   :padding-bottom 20
   :padding-top    (when-not tags? 20)})

(def request-to-join-as
  {:padding-left   20
   :padding-right  20
   :padding-top    4
   :padding-bottom 25})

(defn channel-listing
  [theme height]
  {:background-color (colors/theme-colors colors/white colors/neutral-95 theme)
   :height           height
   :z-index          1})

(defn category-divider
  [theme]
  {:background-color (colors/theme-colors colors/white colors/neutral-95 theme)})

(def community-sheet-position
  {:top (+ -20 -40)})
