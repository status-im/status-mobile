(ns status-im2.contexts.wallet.account.tabs.dapps.style
  (:require
    [quo2.foundations.colors :as colors]))

(def dapps-container
  {:padding-horizontal 20
   :padding-vertical   8})

(defn dapps-list
  [theme]
  {:border-radius 16
   :border-width  1
   :border-color  (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})

(defn separator
  [theme]
  {:height           1
   :background-color (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})
