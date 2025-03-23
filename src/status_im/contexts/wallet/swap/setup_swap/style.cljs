(ns status-im.contexts.wallet.swap.setup-swap.style
  (:require [quo.foundations.colors :as colors]))

(def container
  {:flex 1})

(def keyboard-container
  {:align-self :flex-end
   :width      "100%"})

(def inputs-container
  {:padding-top        12
   :padding-horizontal 20})

(def details-container
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-top        7
   :padding-horizontal 20})

(def detail-item
  {:flex             1
   :height           36
   :background-color :transparent})

(defn swap-order-button
  [approval-required?]
  {:margin-top (if approval-required? 3 -9)
   :z-index    2
   :align-self :center})

(defn receive-token-swap-input-container
  [approval-required?]
  {:margin-top (if approval-required? 3 -9)})

(def footer-container
  {:flex            1
   :justify-content :flex-end})

(def alert-banner
  {:height     :auto
   :min-height 40
   :max-height 62})

(defn exchange-rate-loader
  [theme]
  {:margin-top       16
   :width            72
   :height           14
   :border-radius    6
   :background-color (colors/theme-colors colors/neutral-5 colors/neutral-90 theme)})

(def exchange-rate-container
  {:margin-top     16
   :flex-direction :row})

(def exchange-rate-crypto-label
  {:color colors/neutral-50})

(defn exchange-rate-fiat-label
  [theme]
  {:color (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)})
