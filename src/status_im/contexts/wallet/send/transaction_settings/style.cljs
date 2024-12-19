(ns status-im.contexts.wallet.send.transaction-settings.style
  (:require
    [quo.foundations.colors :as colors]))

(defn prop-text
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})

(def content-line
  {:flex-direction  :row
   :margin-top      2
   :align-items     :center
   :column-gap      4
   :justify-content :flex-start})
