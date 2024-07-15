(ns status-im.contexts.wallet.wallet-connect.session-proposal.style
  (:require [quo.foundations.colors :as colors]))

(def dapp-avatar
  {:padding-horizontal 20
   :padding-top        12})

(def approval-note-container
  {:margin-horizontal  20
   :padding-horizontal 16
   :padding-vertical   12
   :border-radius      16
   :border-width       1
   :border-color       colors/neutral-10
   :background-color   colors/neutral-2_5})

(def approval-note-title
  {:color         colors/neutral-50
   :margin-bottom 8})

(def approval-note-li
  {:flex           1
   :flex-direction :row
   :align-items    :center
   :gap            8})

(def account-switcher-title
  {:padding-horizontal 20})

(def account-switcher-list
  {:margin-top         8
   :padding-horizontal 8})

(def footer-buttons-container
  {:padding-horizontal 0})
