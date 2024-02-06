(ns status-im.contexts.profile.edit.accent-colour.style
  (:require [quo.foundations.colors :as colors]))

(defn page-wrapper
  [insets]
  {:padding-top        (:top insets)
   :padding-bottom     (:bottom insets)
   :padding-horizontal 1
   :flex               1})

(def screen-container
  {:flex            1
   :padding-top     14
   :padding-left    20
   :justify-content :space-between})

(def padding-right
  {:padding-right 20})

(def color-title
  {:color         colors/white-70-blur
   :margin-top    20
   :margin-bottom 16})

(def button-wrapper
  {:margin-vertical 12
   :padding-right   20})

(def profile-card
  {:margin-top    22
   :margin-bottom 5})
