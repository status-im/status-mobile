(ns status-im.contexts.wallet.common.sheets.account-origin.style
  (:require [react-native.platform :as platform]))

(def header-container
  {:gap                2
   :padding-horizontal 20
   :padding-top        12
   :padding-bottom     6})

(def desc-container
  {:gap                2
   :padding-horizontal 20
   :padding-top        0
   :padding-bottom     8})

(def action-container
  {:padding-horizontal 20
   :padding-top        21
   :padding-bottom     (if platform/ios? 14 24)
   :align-self         :flex-start
   :justify-content    :center})
