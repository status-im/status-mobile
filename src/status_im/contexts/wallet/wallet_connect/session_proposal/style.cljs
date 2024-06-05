(ns status-im.contexts.wallet.wallet-connect.session-proposal.style
  (:require [quo.foundations.colors :as colors]))

(def dapp-avatar
  {:padding-horizontal 20
   :padding-top        12})

(def approval-note-container
  {:margin-horizontal 20
   :padding           12
   :border-radius     16
   :border-width      1
   :border-color      colors/neutral-10
   :background-color  colors/neutral-2_5})

(def approval-note-title
  {:color         colors/neutral-50
   :margin-bottom 8})

(def approval-note-li
  {:flex           1
   :flex-direction :row
   :align-items    :center})

(def approval-li-spacer
  {:width 8})

(def detail-item
  {:margin-bottom     20
   :margin-horizontal 20
   :padding           12})
