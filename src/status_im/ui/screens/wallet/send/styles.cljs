(ns status-im.ui.screens.wallet.send.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.wallet.components.styles :as wallet.components.styles]))

(def send-transaction-form
  {:flex           1
   :padding-bottom 60})

(def signing-phrase-description
  {:padding-top 8})

(def password-container
  {:flex             1
   :padding-vertical 20})

(def password
  {:padding 0
   :height  20})

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
   :background-color colors/black-transparent
   :border-radius    50
   :padding          8
   :align-items      :center})

(def advanced-label
  {:text-align-vertical :center
   :margin-left         4})

(def transaction-fee-info
  {:flex-direction   :row
   :margin           15})

(def transaction-fee-input
  {:keyboard-type          :numeric
   :auto-capitalize        "none"
   :placeholder            "0.000"
   :placeholder-text-color colors/white-transparent
   :selection-color        colors/white
   :style                  wallet.components.styles/text-input})