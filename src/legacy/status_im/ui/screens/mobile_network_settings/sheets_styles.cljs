(ns legacy.status-im.ui.screens.mobile-network-settings.sheets-styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]))

(def title
  {:height     21
   :margin-top 8})

(def title-text
  {:typography :title-bold})

(def details
  {:width         311
   :margin-left   32
   :margin-right  32
   :margin-top    6
   :margin-bottom 10})

(def details-text
  {:color      colors/gray
   :text-align :center})

(def separator
  {:background-color colors/gray-lighter
   :margin-left      72
   :align-self       :stretch
   :height           1
   :margin-top       8})

(def checkbox-line-container
  {:margin-left     71
   :margin-top      13
   :height          29
   :flex-direction  :row
   :justify-content :center})

(def checkbox
  {:padding         0
   :justify-content :center
   :align-items     :center
   :width           18
   :height          18
   :border-radius   2
   :margin-top      6})

(def checkbox-icon
  {:tint-color colors/white})

(def checkbox-text-container
  {:justify-content :center
   :flex            1
   :margin-left     13})

(def settings-container
  {:margin-left 69
   :height      44
   :margin-top  6
   :align-items :flex-start})

(def settings-text
  {:color colors/gray})

(def settings-link
  {:color colors/blue})

(def go-to-settings-container
  {:height          52
   :margin-left     72
   :justify-content :center
   :align-self      :stretch})

(def go-to-settings
  {:color colors/blue})
