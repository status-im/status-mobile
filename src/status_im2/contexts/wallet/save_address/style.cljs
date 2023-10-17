(ns status-im2.contexts.wallet.save-address.style
  (:require
    [quo2.foundations.colors :as colors]))

(def color-picker-container
  {:padding-vertical 12})

(defn color-label
  [theme]
  {:color              (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :padding-bottom     4
   :padding-horizontal 20})

