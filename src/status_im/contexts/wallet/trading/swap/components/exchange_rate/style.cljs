(ns status-im.contexts.wallet.trading.swap.components.exchange-rate.style
  (:require [quo.foundations.colors :as colors]))

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
