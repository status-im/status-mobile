(ns status-im.contexts.wallet.connected-dapps.style
  (:require [quo.foundations.colors :as colors]))

(def screen-padding 20)

(def header-container
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-horizontal screen-padding
   :margin-vertical    12})

(def header-wrapper
  {:flex-direction     :column
   :height             96
   :gap                8
   :padding-horizontal 20
   :padding-vertical   12})

(def account-details-wrapper
  {:align-items :flex-start})

(def empty-container-style
  {:justify-content :center
   :flex            1
   :margin-bottom   44})

(defn dapps-container
  [bottom]
  {:padding-horizontal 20
   :padding-vertical   8
   :margin-bottom      bottom
   :flex               1})

(defn dapps-list
  [theme]
  {:border-radius 16
   :border-width  1
   :border-color  (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})

(defn separator
  [theme]
  {:height           1
   :background-color (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})
