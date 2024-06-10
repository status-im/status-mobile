(ns status-im.contexts.settings.wallet.saved-addresses.add-address-to-save.style
  (:require [quo.foundations.colors :as colors]))

(def header-container
  {:padding-bottom 8})

(def input-container
  {:flex-direction    :row
   :margin-horizontal 20})

(def input
  {:flex         1
   :margin-right 12})

(def scan-button
  {:margin-top 26})

(def info-message
  {:margin-top  8
   :margin-left 20})

(def existing-saved-address-container
  {:padding-horizontal 20
   :padding-vertical   12})

(def existing-saved-address-text
  {:color colors/white-opa-40})

(def saved-address-item
  {:margin-top 4})
