(ns status-im.contexts.wallet.wallet-connect.session-proposal.style
  (:require [quo.foundations.colors :as colors]))

(def dapp-avatar
  {:padding-horizontal 20
   :padding-top        12})

(defn approval-note-container
  [theme]
  {:margin-horizontal  20
   :padding-horizontal 16
   :padding-vertical   12
   :border-radius      16
   :border-width       1
   :border-color       (colors/theme-colors colors/neutral-10 colors/black-opa-30 theme)
   :background-color   (colors/theme-colors colors/neutral-2_5 colors/black-opa-30 theme)})

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
