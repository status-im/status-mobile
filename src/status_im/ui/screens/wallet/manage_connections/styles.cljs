(ns status-im.ui.screens.wallet.manage-connections.styles
  (:require [quo.animated :as animated]
            [quo.design-system.colors :as colors]
            [quo2.foundations.colors :as colors-latest]))

(def daap-icon
  {:width            30
   :height           30
   :resize-mode      :cover
   :margin           10
   :border-radius    15
   :border-width     2
   :padding          10})

(def app-row
  {:flex-direction  :row
   :margin-vertical 8
   :align-items     :center})

(def app-column
  {:flex-direction  :column
   :justify-content :center})

(def daap-name
  {:font-size 15})

(def daap-url
  {:font-size 15
   :opacity   0.5})

(def selected-account
  {:font-size 13
   :color   colors/white})

(defn selected-account-container [account-background-color]
  {:background-color   account-background-color
   :padding-horizontal 10
   :justify-content    :center
   :flex-direction     :row
   :align-items        :center
   :border-radius      40
   :height             26})

(def delete-icon-container
  {:margin-horizontal 10})
