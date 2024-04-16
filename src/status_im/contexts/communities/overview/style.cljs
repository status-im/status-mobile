(ns status-im.contexts.communities.overview.style
  (:require
    [quo.foundations.colors :as colors]
    [status-im.contexts.shell.jump-to.constants :as jump-to.constants]))

(def screen-horizontal-padding 20)

(def last-community-tag
  {:margin-right (* 2 screen-horizontal-padding)})

(def community-tag-container
  {:padding-horizontal screen-horizontal-padding
   :margin-horizontal  (- screen-horizontal-padding)
   :margin-bottom      16})

(def community-content-container
  {:padding-horizontal screen-horizontal-padding})

(def preview-user
  {:flex-direction :row
   :align-items    :center
   :margin-top     20})

(def fetching-placeholder
  {:align-items     :center
   :justify-content :center
   :flex            1})

(def fetching-text {:color :red})

(def blur-channel-header
  {:position :absolute
   :top      100
   :height   34
   :right    0
   :left     0
   :flex     1})

(def review-notice
  {:color        colors/neutral-50
   :margin-top   12
   :margin-left  :auto
   :margin-right :auto})

(def community-overview-container
  {:position :absolute
   :top      0
   :left     0
   :right    0
   :bottom   0})

(def floating-shell-button
  {:position :absolute
   :bottom   21})

(defn channel-list-component
  []
  {:margin-top    8
   :margin-bottom (+ 21 jump-to.constants/floating-shell-button-height)
   :flex          1})

(defn token-gated-container
  []
  {:border-radius 16
   :border-color  (colors/theme-colors colors/neutral-20 colors/neutral-80)
   :border-width  1
   :padding-top   10
   :margin-bottom 106})
