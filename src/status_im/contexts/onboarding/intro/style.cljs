(ns status-im.contexts.onboarding.intro.style
  (:require
    [quo.foundations.colors :as colors]))

(def page-container
  {:flex            1
   :justify-content :flex-end})

(def text-container
  {:flex-direction :row
   :flex-wrap      :wrap})

(def text-aligner
  {:flex            1
   :flex-direction  :row
   :justify-content :center})

(def terms-privacy-container
  {:flex-direction     :row
   :padding-horizontal 20
   :padding-vertical   8
   :gap                8})

(def plain-text
  {:color colors/white-opa-70})

(def highlighted-text
  {:color colors/white})

(defn bottom-actions-container
  [bottom-insets]
  {:background-color colors/onboarding-header-black
   :flex-shrink      1
   :padding-bottom   (+ 20 bottom-insets)})
