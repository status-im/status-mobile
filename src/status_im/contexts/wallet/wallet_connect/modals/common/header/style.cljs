(ns status-im.contexts.wallet.wallet-connect.modals.common.header.style
  (:require [quo.foundations.typography :as typography]))

(def ^:private line-height (:line-height typography/heading-1))

(def header-container
  {:padding-vertical 12
   :margin-left      -4
   :justify-content  :flex-start
   :align-items      :center
   :flex-direction   :row
   :flex-wrap        :wrap
   :row-gap          2})

(def word-container
  {:height          line-height
   :justify-content :center})

(def dapp-container
  {:margin-top        0
   :height            line-height
   :margin-horizontal 4})
