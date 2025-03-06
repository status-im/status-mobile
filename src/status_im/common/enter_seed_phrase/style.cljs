(ns status-im.common.enter-seed-phrase.style
  (:require [react-native.platform :as platform]))

(def full-layout {:flex 1})

(def page-container
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(defn recovery-phrase-container
  [{:keys [banner-offset insets keyboard-shown?]}]
  {:flex           1
   :padding-bottom (if keyboard-shown?
                     (when platform/ios? banner-offset)
                     (:bottom insets))})

(def form-container
  {:flex               1
   :padding-horizontal 20
   :padding-vertical   12})

(def header-container
  {:flex-direction  :row
   :justify-content :space-between})

(def word-count-container
  {:justify-content :flex-end
   :margin-bottom   2})

(def input-container
  {:flex              1
   :margin-top        12
   :margin-horizontal -20})

(def continue-button
  {:margin-top :auto})

(def keyboard-container {:margin-top :auto})
