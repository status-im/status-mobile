(ns status-im2.contexts.syncing.syncing-devices-list.style
  (:require [quo2.foundations.colors :as colors]))

(def container-main
  {:background-color colors/neutral-95
   :flex             1})

(def page-container
  {:flex              1
   :justify-content   :flex-start
   :margin-horizontal 20})

(def title-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between
   :margin-top      12
   :margin-bottom   12})

(def navigation-bar
  {:height 56})

(def subtitle
  {:margin-top 20
   :color      colors/white-opa-40})
