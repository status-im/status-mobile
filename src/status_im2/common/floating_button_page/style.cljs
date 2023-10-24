(ns status-im2.common.floating-button-page.style
  (:require [react-native.platform :as platform]
            [status-im2.common.floating-button-page.constants :as constants]))

(def page-container
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0
   :z-index  100})

(def keyboard-view-style
  {:border-width 1
   :border-color :blue
   :padding-bottom 12})

