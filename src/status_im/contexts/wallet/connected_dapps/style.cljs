(ns status-im.contexts.wallet.connected-dapps.style
  (:require [quo.foundations.colors :as colors]))

(def screen-padding 20)

(def header-container
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-horizontal screen-padding
   :margin-vertical    12})

(defn header-text
  [bottom-padding?]
  {:padding-horizontal screen-padding
   :padding-top        12
   :padding-bottom     (when bottom-padding? 8)
   :color              colors/black})
