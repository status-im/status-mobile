(ns status-im2.contexts.wallet.create-account.style
  (:require [quo2.foundations.colors :as colors]))

(defn gradient-cover-container
  [top]
  {:position :absolute
   :top      (- top)
   :left     0
   :right    0
   :z-index  -1})

(def account-avatar-container
  {:padding-horizontal 20
   :padding-top        12})

(def reaction-button-container
  {:position :absolute
   :bottom   0
   :left     80})

(def title-input-container
  {:padding-horizontal 20
   :padding-top        12
   :padding-bottom     16})

(def color-picker-container
  {:padding-vertical 12})

(defn color-label
  [theme]
  {:color              (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :padding-bottom     4
   :padding-horizontal 20})

(def divider-line
  {:margin-top    12
   :margin-bottom 8})

(defn slide-button-container
  [bottom]
  {:position :absolute
   :bottom   (+ bottom 12)
   :left     20
   :right    20})
