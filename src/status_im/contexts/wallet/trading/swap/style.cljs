(ns status-im.contexts.wallet.trading.swap.style
  (:require [quo.foundations.colors :as colors]
            [react-native.safe-area :as safe-area]))

(defn home-container
  []
  {:margin-top 8
   :flex       1})

(defn top-nav-container
  []
  {:margin-top (+ (safe-area/get-top) 0)})

(defn header-container
  [theme]
  {:background-color (colors/theme-colors colors/white colors/neutral-95 theme)})

(def keyboard-container
  {:align-self :flex-end
   :width      "100%"})

(def inputs-container
  {:padding-top        12
   :padding-horizontal 20})

(defn receive-token-swap-input-container
  [approval-required?]
  {:margin-top (if approval-required? 3 -9)})

(defn swap-order-button
  [approval-required?]
  {:margin-top (if approval-required? 3 -9)
   :z-index    2
   :align-self :center})

(def alert-banner
  {:height     :auto
   :min-height 40
   :max-height 62})

(def footer-container
  {:flex            1
   :justify-content :flex-end})

(def info-bar
  {:padding-top        12
   :padding-horizontal 20})

(def detail-item
  {:flex             1
   :height           36
   :background-color :transparent})
