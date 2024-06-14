(ns status-im.contexts.onboarding.intro.style
  (:require
    [quo.foundations.colors :as colors]))

(def page-container
  {:flex            1
   :justify-content :flex-end})

(def text-container
  {:flex       1
   :text-align :center
   :margin-top 16
   :flex-wrap  :wrap})

(def plain-text
  {:color colors/white-opa-70})

(def highlighted-text
  {:flex  1
   :color colors/white})

(def bottom-actions-container
  {:position         :absolute
   :background-color colors/onboarding-header-black
   :left             0
   :right            0
   :padding-bottom   40})
