(ns status-im.contexts.onboarding.intro.style
  (:require
    [quo.foundations.colors :as colors]))

(def page-container
  {:flex            1
   :justify-content :flex-end})

(def terms-privacy-container
  {:padding-horizontal 20
   :padding-vertical   8
   :align-items        :center})

(def plain-text
  {:color colors/white-opa-70})

(def highlighted-text
  {:color colors/white})

(defn bottom-actions-container
  [bottom-insets]
  {:background-color colors/onboarding-header-black
   :flex-shrink      1
   :padding-bottom   (+ 20 bottom-insets)})
