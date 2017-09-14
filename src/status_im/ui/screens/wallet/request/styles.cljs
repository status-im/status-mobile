(ns status-im.ui.screens.wallet.request.styles
  (:require [status-im.components.styles :as styles]))

(def main-container
  {:flex 1})

(def network-label
  {:margin-top 27})

(def network-container
  {:flex        1
   :align-items :center})

(def qr-container
  {:margin-top 16})

(def choose-wallet-container
  {:margin-top        27
   :margin-horizontal 15})

(def amount-container
  {:margin-top        16
   :margin-horizontal 15
   :flex-direction    :row})

(def choose-currency-container
  {:margin-left 8})

(def choose-currency
  {:width 116})

(def separator
  {:height            1
   :margin-horizontal 15
   :background-color  styles/color-white-transparent-1
   :margin-top        16})

(def buttons-container
  {:margin-vertical    15
   :padding-horizontal 12
   :flex-direction     :row
   :align-items        :center})

(def share-icon-container
  {:margin-right 8})

(def forward-icon-container
  {:margin-left 8})

(def button-text
  {:color          :white
   :font-size      15
   :letter-spacing -0.2})

(def button-container
  {:flex-direction :row
   :align-items    :center})