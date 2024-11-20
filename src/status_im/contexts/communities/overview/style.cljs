(ns status-im.contexts.communities.overview.style
  (:require
    [status-im.contexts.shell.constants :as shell.constants]))

(def screen-horizontal-padding 20)

(def last-community-tag
  {:margin-right (* 2 screen-horizontal-padding)})

(def community-tag-container
  {:padding-horizontal screen-horizontal-padding
   :margin-horizontal  (- screen-horizontal-padding)
   :margin-bottom      16})

(def community-content-container
  {:padding-horizontal screen-horizontal-padding})

(defn fetching-placeholder
  [top-inset]
  {:flex       1
   :margin-top top-inset})

(def blur-channel-header
  {:position :absolute
   :top      100
   :height   34
   :right    0
   :left     0
   :flex     1})

(def community-overview-container
  {:position :absolute
   :top      0
   :left     0
   :right    0
   :bottom   0})

(defn channel-list-component
  []
  {:margin-top    8
   :margin-bottom (+ 21 shell.constants/floating-shell-button-height)
   :flex          1})
