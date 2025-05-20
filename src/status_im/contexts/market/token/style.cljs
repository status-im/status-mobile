(ns status-im.contexts.market.token.style
  (:require [quo.foundations.colors :as colors]))

(defn header-page-nav
  [theme]
  {:background-color (colors/theme-colors colors/neutral-2_5 colors/neutral-90 theme)})

(defn header-top
  [theme]
  {:padding-vertical   12
   :padding-horizontal 20
   :row-gap            8
   :background-color   (colors/theme-colors colors/neutral-2_5 colors/neutral-90 theme)})

(def header-title-row
  {:flex           1
   :flex-direction :row
   :align-items    :flex-end
   :column-gap     8})

(def header-token-name-text
  {})

(defn header-token-ticker-text
  [theme]
  {:color          (colors/theme-colors colors/neutral-50 colors/neutral-50 theme)
   :padding-bottom 1})

(def header-buttons
  {:flex-direction :row
   :display        :flex
   :column-gap     12
   :margin-top     12
   :margin-bottom  4})

(def header-button
  {:flex 1})

(defn content-container
  [theme]
  {:border-top-width 1
   :border-top-color (colors/theme-colors colors/neutral-10 colors/neutral-90 theme)
   :padding-left     20
   :padding-right    20
   :padding-top      8
   :column-gap       12})

(def content-row
  {:flex-direction :row})

(def token-overview
  {:flex           1
   :padding-top    12
   :padding-bottom 16})

(def token-overview-info-row
  {:flex-direction :row
   :column-gap     6
   :align-items    :center})

(defn token-overview-icon-props
  [theme positive?]
  {:color (if positive?
            (colors/theme-colors colors/success-50 colors/success-60 theme)
            (colors/theme-colors colors/danger-50 colors/danger-60 theme))
   :size  16})

(defn token-overview-change-text
  [theme positive?]
  {:color (if positive?
            (colors/theme-colors colors/success-50 colors/success-60 theme)
            (colors/theme-colors colors/danger-50 colors/danger-60 theme))})

(defn token-overview-change-time-text
  [theme]
  {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)})

(defn token-parameter
  [first?]
  {:flex           1
   :padding-left   (if first? 0 16)
   :padding-right  16
   :padding-top    12
   :padding-bottom 12})

(defn token-parameter-title
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-50 theme)})

(defn token-parameter-value
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})

(defn dashed-line-outer-container
  [theme type]
  {:width         (if (= type :horizontal) "100%" 1)
   :height        (if (= type :horizontal) 1 "100%")
   :border-radius 1
   :border-width  1
   :border-style  :dashed
   :border-color  (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :z-index       0})

(defn dashed-line-inner-container
  [theme type]
  {:position         :absolute
   :left             (if (= type :horizontal) -1 0)
   :right            (if (= type :horizontal) -1 0)
   :top              (if (= type :horizontal) 0 -1)
   :bottom           (if (= type :horizontal) 0 -1)
   :height           (if (= type :horizontal) 1 "100%")
   :width            (if (= type :horizontal) "100%" 1)
   :background-color (colors/theme-colors colors/white colors/neutral-95 theme)
   :z-index          1})
