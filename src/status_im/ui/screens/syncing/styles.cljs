(ns status-im.ui.screens.syncing.styles
  (:require [quo2.foundations.colors :as colors]))

(def synced-devices-text
  {:font-size 13
   :font-weight :500
   :color colors/neutral-40})

(def container-main
  {:margin 16})

(def devices-container
  {:border-color colors/neutral-20
   :border-radius 16
   :border-width 1
   :margin-top 12})

(def device-row
  {:flex-direction :row
   :padding-vertical 10
   :padding-left 10})

(def device-name
  {:font-size 15
   :font-weight :500
   :color colors/black})

(def device-status
  {:font-size 13
   :font-weight :400
   :color colors/neutral-40})

(def device-column
  {:flex-direction :column
   :margin-left 10
   :align-self :center})

(def sync-device-container
  {:padding 10})










