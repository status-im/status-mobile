(ns status-im.contexts.communities.actions.accounts-selection.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.safe-area :as safe-area]))

(def screen-horizontal-padding 20)

(def container
  {:flex 1})

(def section-title
  {:padding-top        12
   :padding-bottom     4
   :padding-horizontal screen-horizontal-padding})

(defn bottom-actions
  []
  {:position           :absolute
   :background-color   (colors/theme-colors colors/white colors/neutral-95)
   :padding-bottom     (safe-area/get-bottom)
   :bottom             0
   :left               0
   :right              0
   :padding-horizontal screen-horizontal-padding
   :padding-top        12
   :flex               1})
