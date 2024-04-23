(ns legacy.status-im.ui.screens.group.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]))

(def group-container
  {:flex           1
   :flex-direction :column})

(defn no-contact-text
  []
  {:margin-bottom     20
   :margin-horizontal 50
   :text-align        :center
   :color             colors/gray})

(def no-contacts
  {:flex            1
   :justify-content :center
   :align-items     :center})

(defn search-container
  []
  {:border-bottom-color colors/gray-lighter
   :border-bottom-width 1
   :padding-horizontal  16
   :padding-vertical    10})

(defn members-title
  []
  {:color         colors/gray
   :margin-top    14
   :margin-bottom 4})
