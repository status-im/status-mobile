(ns status-im.contexts.communities.actions.accounts-selection.style
  (:require
    [quo.foundations.colors :as colors]))

(def screen-horizontal-padding 20)

(def container
  {:flex 1})

(def page-top
  {:padding-vertical   12
   :padding-horizontal screen-horizontal-padding})

(def section-title
  {:padding-top        12
   :padding-bottom     4
   :padding-horizontal screen-horizontal-padding})

(defn bottom-actions
  []
  {:position           :absolute
   :background-color   (colors/theme-colors colors/white colors/neutral-95)
   :bottom             0
   :left               0
   :right              0
   :padding-horizontal screen-horizontal-padding
   :padding-vertical   12
   :flex               1})
