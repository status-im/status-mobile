(ns status-im2.common.syncing.style
  (:require [quo2.foundations.colors :as colors]))

(def device-container
  {:padding-top        12
   :padding-horizontal 12
   :padding-bottom     16
   :border-color       colors/white-opa-5
   :border-radius      16
   :border-width       1
   :margin-bottom      24})

(def device-container-orientation
  {:flex-direction :row})

(def icon-container
  {:height       20
   :margin-right 12})

(def tag-container
  {:margin-top 8})

(def render-device-status
  {:background-color colors/success-60
   :align-self       :center
   :width            8
   :height           8
   :border-radius    4
   :margin-right     6})
