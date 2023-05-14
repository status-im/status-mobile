(ns status-im2.contexts.onboarding.new-to-status.style
  (:require [quo2.foundations.colors :as colors]))

(def full-screen {:flex 1})

(def content-container
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(def options-container
  {:padding-top        12
   :padding-horizontal 20})

(def title
  {:color         colors/white
   :margin-bottom 20})

(def subtitle-container
  {:height        20
   :margin-top    16
   :margin-bottom 4})

(def subtitle
  {:color colors/white-opa-70})

(def suboptions
  {:padding-top    4
   :padding-bottom 8})

(def space-between-suboptions {:height 12})
