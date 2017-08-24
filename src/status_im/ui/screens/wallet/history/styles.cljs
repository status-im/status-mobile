(ns status-im.ui.screens.wallet.history.styles
  (:require [status-im.components.styles :as st]))

(def wallet-transactions-container
  {:flex             1
   :background-color st/color-white})

(def main-section
  {:flex             1
   :position         :relative
   :background-color st/color-white})

(def empty-text
  {:text-align       :center
   :margin-top        22
   :margin-horizontal 92})

(def action-buttons
  {:flex             1
   :flex-direction   :row
   :padding-vertical 12})

(def sign-all-view
  {:flex              1
   :flex-direction    :column
   :justify-content   :center
   :background-color  st/color-gray-transparent})

(def sign-all-popup
  {:align-self        :flex-start
   :background-color  st/color-white
   :margin-horizontal 12
   :border-radius     8})

(def sign-all-popup-sign-phrase
  {:border-radius     8
   :margin-top        12
   :margin-horizontal 12
   :text-align        :center
   :padding-vertical  9
   :background-color  st/color-light-gray})

(def sign-all-popup-text
  {:margin-top        8
   :margin-horizontal 12})

(def sign-all-actions
  {:flex-direction    :row
   :justify-content   :space-between
   :margin-horizontal 12
   :margin-vertical   20})

(def sign-all-input
  {:width  150
   :height 38})

(def sign-all-done
  {:position :absolute
   :right    0
   :top      0})

(def sign-all-done-button
  {:background-color :transparent})