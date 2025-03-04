(ns status-im.contexts.wallet.trading.swap.approval-confirmation.style
  (:require [quo.foundations.colors :as colors]))

(def summary-section-container
  {:padding-horizontal 20
   :padding-bottom     16})

(defn section-label
  [theme]
  {:margin-bottom 8
   :color         (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})
