(ns status-im.contexts.onboarding.intro.style
  (:require
    [quo.foundations.colors :as colors]))

(def page-container
  {:flex            1
   :justify-content :flex-end})

(def text-container
  {:flex       1
   :flex-wrap  :wrap
   :text-align :center})

(def terms-privacy-container
  {:gap                8
   :padding-horizontal 20
   :padding-vertical   8})

(def plain-text
  {:color colors/white-opa-70})

(def highlighted-text
  {:flex  1
   :color colors/white})

(defn bottom-actions-container
  [bottom-insets]
  {:background-color colors/onboarding-header-black
   :flex-shrink      1
   :padding-bottom   (+ 20 bottom-insets)})
