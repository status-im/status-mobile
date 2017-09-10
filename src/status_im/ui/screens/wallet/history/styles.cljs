(ns status-im.ui.screens.wallet.history.styles
  (:require [status-im.components.styles :as styles]))

(def error-container
  {:align-self       :center
   :justify-content  :center
   :border-radius    4
   :padding-vertical 4
   :flex-direction   :row
   :background-color styles/color-gray9})

(def error-message
  {:color         styles/color-black
   :padding-top   3
   :padding-right 10
   :font-size     13})

(def main-section styles/flex)

(def tabs
  {:border-bottom-width 1
   :border-bottom-color styles/color-gray10-transparent})

(def tab-active
  {:border-bottom-width 2
   :border-bottom-color styles/color-blue4})

(def forward
  {:color styles/color-gray7})

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
   :background-color  styles/color-gray-transparent})

(def sign-all-popup
  {:align-self        :flex-start
   :background-color  styles/color-white
   :margin-horizontal 12
   :border-radius     8})

(def sign-all-popup-sign-phrase
  {:border-radius     8
   :margin-top        12
   :margin-horizontal 12
   :text-align        :center
   :padding-vertical  9
   :background-color  styles/color-light-gray})

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

(defn transaction-icon-background [color]
  {:flex             1
   :justify-content  :center
   :align-items      :center
   :width            40
   :height           40
   :border-radius    32
   :background-color color})
