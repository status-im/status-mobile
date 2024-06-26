(ns status-im.contexts.onboarding.create-or-sync-profile.style
  (:require
    [quo.foundations.colors :as colors]))

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

(def subtitle
  {:margin-bottom 12
   :color         colors/white-opa-70})

(def subtitle-container
  {:margin-top 24})

(def space-between-suboptions {:height 12})
