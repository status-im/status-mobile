(ns status-im2.contexts.communities.overview.style
  (:require [quo2.foundations.colors :as colors]))

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

(def join-button
  {:width        "100%"
   :margin-top   20
   :margin-left  :auto
   :margin-right :auto})

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
