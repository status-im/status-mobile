(ns status-im.ui.screens.wallet.send.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as styles]
            [status-im.ui.screens.wallet.components.styles :as wallet.components.styles]))

(def send-transaction-form
  {:flex           1
   :padding-bottom 60})

(defn animated-sign-panel [bottom-value]
  {:position           :absolute
   :left               12
   :right              12
   :bottom             bottom-value})

(defn sign-panel [opacity-value]
  {:opacity            opacity-value
   :border-radius      8
   :background-color   colors/white
   :padding-top        12
   :padding-horizontal 12})

(def signing-phrase-container
  {:border-radius    8
   :height           36
   :align-items      :center
   :justify-content  :center
   :background-color colors/gray-lighter})

(def signing-phrase
  {:font-size      15
   :letter-spacing -0.2
   :color          colors/black})

(def signing-phrase-description
  {:padding-top 8})

(def password-container
  {:flex             1
   :padding-vertical 20})

(def password
  {:padding        0
   :font-size      15
   :letter-spacing -0.2
   :height         20})

(def processing-view
  {:position         :absolute
   :top              0
   :bottom           0
   :right            0
   :left             0
   :align-items      :center
   :justify-content  :center
   :background-color (str colors/black "1A")})

(def empty-text
  {:text-align        :center
   :margin-top        22
   :margin-horizontal 92})

(def advanced-cartouche
  {:padding-bottom 50})

(def advanced-button
  {:flex-direction   :row
   :background-color styles/color-blue6
   :border-radius    50
   :padding          8
   :align-items      :center})

(def advanced-button-wrapper
  {:align-items :center})

(def advanced-wrapper
  {:margin-top    24
   :margin-bottom 16})

(def advanced-options-wrapper
  {:align-items    :center
   :flex-direction :row})

(def advanced-options-text-wrapper
  {:flex            1
   :flex-direction  :row
   :justify-content :space-between
   :margin-vertical 15})

(def advanced-label
  {:text-align-vertical :center
   :margin-left         4})

(def advanced-fees-text
  {:color colors/white})

(def advanced-fees-details-text
  {:color colors/white-lighter-transparent})

(def transaction-fee-block-wrapper
  {:flex-direction :row})

(def transaction-fee-info
  {:margin 15})

(def transaction-fee-input
  {:keyboard-type          :numeric
   :auto-capitalize        "none"
   :placeholder            "0.000"
   :placeholder-text-color colors/white-lighter-transparent
   :selection-color        colors/white
   :style                  wallet.components.styles/text-input})

(def sign-buttons
  {:background-color colors/blue
   :padding-vertical 8})

(def fee-buttons
  {:background-color colors/blue})
