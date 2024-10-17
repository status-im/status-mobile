(ns status-im.contexts.syncing.syncing-devices-list.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container-main
  [top]
  {:background-color colors/neutral-95
   :padding-top      top
   :flex             1})

(def page-container
  {:flex               1
   :padding-horizontal 20})

(def title-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between
   :margin-top      12
   :margin-bottom   12})

(def subtitle
  {:margin-top 20
   :color      colors/white-opa-40})
