(ns status-im.contexts.wallet.send.routes.style
  (:require [quo.foundations.colors :as colors]))

(def routes-container
  {:padding-horizontal 20
   :flex               1
   :padding-vertical   16
   :width              "100%"
   :height             "100%"})

(def routes-header-container
  {:flex-direction  :row
   :justify-content :space-between})

(defn routes-inner-container
  [first-item?]
  {:margin-top      (if first-item? 7.5 11)
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def section-label-right
  {:width 135})

(def section-label-left
  {:width 136})

(def network-link
  {:margin-horizontal -1.5
   :z-index           1
   :flex              1})

(def empty-container
  {:flex-grow       1
   :align-items     :center
   :justify-content :center})

(def add-network
  {:margin-top 11
   :align-self :flex-end})

(defn warning-container
  [color theme]
  {:flex-direction    :row
   :border-width      1
   :border-color      (colors/resolve-color color theme 10)
   :background-color  (colors/resolve-color color theme 5)
   :margin-horizontal 20
   :margin-top        4
   :margin-bottom     8
   :padding-left      12
   :padding-vertical  11
   :border-radius     12})

(def warning-text
  {:margin-left   8
   :margin-right  12
   :padding-right 12})
