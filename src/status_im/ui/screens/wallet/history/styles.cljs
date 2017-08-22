(ns status-im.ui.screens.wallet.history.styles
  (:require [status-im.components.styles :as st]))

(def wallet-transactions-container
  {:flex             1
   :background-color st/color-white})

(def toolbar-right-action
  {:color        st/color-blue4
   :font-size    17
   :margin-right 12})

(def main-section
  {:flex             1
   :position         :relative
   :background-color st/color-white})

(def empty-text
  {:text-align       :center
   :margin-top        22
   :margin-horizontal 92})