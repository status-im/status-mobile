(ns status-im.contexts.profile.settings.screens.notifications.styles
  (:require
    [quo.foundations.colors :as colors]))

(def information-box
  {:margin-horizontal 20
   :margin-vertical   12})

(def information-box-button-label
  {:gap            4
   :align-items    :center
   :flex-direction :row})

(def settings-group-container
  {:margin-horizontal 20
   :margin-vertical   8
   :border-width      1
   :border-radius     20
   :border-color      colors/white-opa-5})

(def settings-group-header
  {:flex-direction   :row
   :padding-left     16
   :padding-right    12
   :padding-vertical 13
   :gap              12})

(def settings-group-item-container
  {:padding-horizontal 8
   :padding-top        0
   :paddong-bottom     0})
