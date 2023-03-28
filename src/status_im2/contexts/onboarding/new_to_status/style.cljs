(ns status-im2.contexts.onboarding.new-to-status.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(def full-screen {:flex 1})

(def content-container
  {:padding-top (if platform/ios? 44 0)
   :position    :absolute
   :top         0
   :bottom      0
   :left        0
   :right       0})

(def options-container
  {:padding-top        12
   :padding-horizontal 20})

(def title
  {:color         colors/white
   :margin-bottom 20})

(def subtitle-container
  {:height         42
   :padding-top    16
   :padding-bottom 8})

(def subtitle
  {:color         colors/white-opa-70
   :margin-bottom 20})

(def suboptions
  {:padding-top    4
   :padding-bottom 8})

(def space-between-suboptions {:height 12})
