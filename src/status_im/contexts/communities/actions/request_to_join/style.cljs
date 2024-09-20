(ns status-im.contexts.communities.actions.request-to-join.style
  (:require
    [quo.foundations.colors :as colors]))

(def page-container
  {:margin-horizontal 20
   :margin-bottom     20})

(def title-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def container
  {:flex 1})

(def community-icon
  {:margin-right :auto :margin-top 4})

(def cancel-button
  {:flex         1
   :margin-right 12})

(def bottom-container
  {:padding-top       32
   :flex-direction    :row
   :align-items       :center
   :margin-horizontal 20
   :justify-content   :space-evenly})

(def final-disclaimer-container
  {:margin-bottom      7
   :margin-top         12
   :padding-horizontal 40})

(def final-disclaimer-text
  {:color      colors/neutral-50
   :text-align :center})

(def rules-text
  {:margin-top 24})
