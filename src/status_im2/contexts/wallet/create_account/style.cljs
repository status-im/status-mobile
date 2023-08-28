(ns status-im2.contexts.wallet.create-account.style
  (:require [quo2.foundations.colors :as colors]))

(defn gradient-cover-container
  [top]
  {:position :absolute
   :top      (- top)
   :left     0
   :right    0
   :z-index  -1})

(def reaction-button-container
  {:position :absolute
   :bottom   0
   :left     80})

(def title-input-container
  {:padding-horizontal 20
   :padding-top        12
   :padding-bottom     16})

(defn divider-line
  [theme]
  {:border-color        (colors/theme-colors colors/neutral-10 colors/neutral-90 theme)
   :padding-top         12
   :padding-bottom      8
   :border-bottom-width 1})

(defn slide-button-container
  [bottom]
  {:position :absolute
   :bottom   (+ bottom 12)
   :left     20
   :right    20})
