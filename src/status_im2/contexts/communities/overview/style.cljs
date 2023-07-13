(ns status-im2.contexts.communities.overview.style
  (:require [quo2.foundations.colors :as colors]))

(def screen-horizontal-padding 20)

(def last-community-tag
  {:margin-right (* 2 screen-horizontal-padding)})

(def community-tag-container
  {:padding-horizontal screen-horizontal-padding
   :margin-horizontal  (- screen-horizontal-padding)})

(def community-content-container
  {:padding-horizontal screen-horizontal-padding})

(def preview-user
  {:flex-direction :row
   :align-items    :center
   :margin-top     20})

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

(defn token-gated-container
  []
  {:border-radius 16
   :border-color  (colors/theme-colors colors/neutral-20 colors/neutral-80)
   :border-width  1
   :padding-top   10})
