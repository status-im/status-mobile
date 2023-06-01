(ns status-im2.contexts.syncing.syncing-devices-list.style
  (:require [quo2.foundations.colors :as colors]))

(def container-main
  {:background-color colors/neutral-95
   :flex             1})

(def page-container
  {:flex              1
   :margin-horizontal 20})

(def title-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between
   :margin-bottom   24})

(def devices-container
  {:flex 1})

(def device-container
  {:display            :flex
   :padding-top        12
   :padding-horizontal 12
   :padding-bottom     16

   :flex-direction     :row
   :border-color       colors/white-opa-5
   :border-radius      16
   :border-width       1
   :height             100
   :margin-top         24})

(def device-details
  {:height 200})

(def navigation-bar
  {:height 56})

(def icon-container
  {:height       20
   :margin-right 12})

(def tag-container
  {:margin-top 8})
