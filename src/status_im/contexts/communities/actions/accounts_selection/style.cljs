(ns status-im.contexts.communities.actions.accounts-selection.style
  (:require
    [quo.foundations.colors :as colors]))

(def screen-horizontal-padding 20)

(def container
  {:flex       1
   :margin-top -12})

(def section-title
  {:padding-top        12
   :padding-bottom     4
   :padding-horizontal screen-horizontal-padding})

(defn bottom-actions
  [theme]
  {:height             48
   :margin-vertical    12
   :padding-horizontal screen-horizontal-padding
   :background-color   (colors/theme-colors colors/white colors/neutral-95 theme)})
