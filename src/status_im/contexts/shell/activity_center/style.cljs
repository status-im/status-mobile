(ns status-im.contexts.shell.activity-center.style
  (:require
    [quo.foundations.colors :as colors]))

(def screen-padding 20)

(def header-container
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-horizontal screen-padding
   :margin-vertical    12})

(def header-heading
  {:padding-horizontal screen-padding
   :padding-vertical   12
   :color              colors/white})

(defn notification-container
  [index]
  {:margin-top (if (zero? index) 0 4)})

(def tabs
  {:padding-left screen-padding})

(def tabs-container
  {:flex       1
   :align-self :stretch})

(def filter-toggle-container
  {:flex-grow     0
   :margin-left   16
   :padding-right screen-padding})

(def tabs-and-filter-container
  {:flex-direction   :row
   :padding-vertical 12})

(def empty-container
  {:align-items     :center
   :flex            1
   :justify-content :center
   :padding-bottom  20})
