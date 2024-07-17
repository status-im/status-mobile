(ns status-im.contexts.onboarding.intro.style
  (:require
    [quo.foundations.colors :as colors]))

(def page-container
  {:flex            1
   :justify-content :flex-end})

(def text-container
  {:text-align :center
   :margin-top 4
   :flex-wrap  :wrap})

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
