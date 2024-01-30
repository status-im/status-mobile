(ns status-im.contexts.profile.edit.names.style
  (:require [quo.foundations.colors :as colors]))

(defn page-wrapper
  [insets]
  {:padding-top        (:top insets)
   :padding-bottom     (:bottom insets)
   :padding-horizontal 1
   :flex               1})

(def screen-container
  {:flex               1
   :padding-top        14
   :padding-horizontal 20
   :justify-content    :space-between})

(def item-container
  {:background-color colors/white-opa-5
   :border-radius    16
   :margin-bottom    4})
