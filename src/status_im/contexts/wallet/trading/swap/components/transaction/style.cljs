(ns status-im.contexts.wallet.trading.swap.components.transaction.style
  (:require [quo.foundations.colors :as colors]))

(def container
  {:flex       1
   :margin-top -20})

(def scroll-view-container
  {:flex           1
   :padding-bottom 20})

(def title-container
  {:padding-top        12
   :padding-horizontal 20
   :padding-bottom     32})

(def title-text
  {:margin-horizontal 4})

(def title-row
  {:flex-direction :row})

(def providers-container
  {:align-items :center
   :margin-top  12})

(defn swaps-powered-by
  [theme]
  {:color (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-70 theme)})

(defn terms-and-conditions
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})
