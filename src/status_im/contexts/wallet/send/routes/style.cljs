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

(def routes-inner-container
  {:margin-top      8
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(defn section-label
  [margin-left]
  {:flex        0.5
   :margin-left margin-left})

(def network-link
  {:right   6
   :z-index 1})

(def empty-container
  {:flex-grow       1
   :align-items     :center
   :justify-content :center})

(def add-network
  {:margin-top 8
   :align-self :flex-end
   :right      12})

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
