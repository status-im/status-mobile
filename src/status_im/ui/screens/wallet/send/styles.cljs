(ns status-im.ui.screens.wallet.send.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as styles]
            [status-im.ui.screens.wallet.components.styles :as wallet.components.styles]))

(def send-transaction-form
  {:padding-bottom 60})

(def spinner-container
  {:position        :absolute
   :left            0
   :top             0
   :right           0
   :bottom          0
   :justify-content :center})

(def signing-phrase-container
  {:flex               1
   :border-radius      200
   :border-width       1
   :margin-vertical    24
   :padding-vertical   12
   :padding-horizontal 25
   :align-items        :center
   :justify-content    :center
   :border-color       colors/gray-light})

(def signing-phrase
  {:font-size      22
   :color          colors/black
   :letter-spacing 10})

(def transaction-amount
  {:flex           1
   :font-size      22
   :letter-spacing 0.35
   :font-weight    :bold
   :text-align     :center
   :line-height    28
   :margin-bottom  24
   :color          colors/black})

(def signing-phrase-description
  {:padding-top 8})

(def password-container
  {:flex               1
   :width              "100%"
   :margin-horizontal  40
   :padding-vertical   17
   :padding-horizontal 14
   :border-radius      8
   :background-color   colors/gray-lighter})

(def password
  {:flex 1
   :padding        0
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

(def gas-container-wrapper
  {:flex           1
   :flex-direction :row})

(def gas-input-wrapper
  {:align-items     :center
   :justify-content :space-between
   :flex-direction  :row})

(def advanced-options-text-wrapper
  {:flex            1
   :flex-direction  :row
   :justify-content :space-between
   :margin-vertical 15})

(def advanced-label
  {:text-align-vertical :center
   :margin-left         4})

(def advanced-fees-text
  {:color            colors/white})

(def advanced-fees-details-text
  {:color colors/white-lighter-transparent})

(def transaction-fee-block-wrapper
  {:flex-direction :row})

(def transaction-fee-info
  {:flex-direction   :row
   :margin           15})

(def transaction-fee-info-text-wrapper
  {:flex-shrink      1})

(def transaction-fee-info-icon
  {:border-radius    25
   :width            25
   :height           25
   :margin-right     15
   :align-items      :center
   :justify-content  :center
   :background-color colors/blue-dark})

(def transaction-fee-info-icon-text
  {:color            colors/white
   :font-size        14})

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

(defn password-error-tooltip [sending?]
  {:bottom-value (if sending? -145 -95)
   :color        colors/red-light})

(defstyle gas-input-error-tooltip
  {:android {:bottom-value -38}})

(def password-button
  {:margin-top         12
   :margin-bottom      30
   :padding-horizontal 25
   :padding-vertical   5})

(def emojis-tooltip
  {:bottom-value   -5
   :font-size      12
   :font-color     colors/white
   :color          colors/blue
   :delay          1500})
