(ns status-im.ui.screens.wallet.send.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.screens.wallet.components.styles :as wallet.components.styles]))

(def toolbar
  {:background-color styles/color-blue5
   :elevation        0
   :padding-bottom   10})

(def toolbar-title-container
  {:flex           1
   :flex-direction :row
   :margin-left    6})

(def toolbar-title-text
  {:color        styles/color-white
   :font-size    17
   :margin-right 4})

(def toolbar-icon
  {:width  24
   :height 24})

(def toolbar-title-icon
  (merge toolbar-icon {:opacity 0.4}))

(defn animated-sign-panel [bottom-value]
  {:position           :absolute
   :left               12
   :right              12
   :bottom             bottom-value})

(defn sign-panel [opacity-value]
  {:opacity            opacity-value
   :border-radius      8
   :background-color   :white
   :padding-top        12
   :padding-horizontal 12})

(def signing-phrase-container
  {:border-radius    8
   :height           36
   :align-items      :center
   :justify-content  :center
   :background-color styles/color-light-gray})

(def signing-phrase
  {:font-size      15
   :letter-spacing -0.2
   :color          :black})

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
   :background-color (str styles/color-black "1A")})

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
  {:margin-horizontal 15})

(def advanced-options-wrapper
  {:height           52
   :background-color styles/color-white-transparent-3
   :border-radius    4
   :margin-top       16
   :margin-bottom    16
   :align-items      :center
   :flex-direction   :row
   :padding-vertical 14
   :padding-right    8
   :ios              {:border-radius 8}
   :android          {:border-radius 4}})

(def advanced-options-text-wrapper
  {:flex              1
   :flex-direction    :row
   :justify-content   :space-between
   :margin-horizontal 15})

(def advanced-label
  {:text-align-vertical :center
   :margin-left         4})

(def advanced-fees-text
  {:color styles/color-white})

(def advanced-fees-details-text
  {:color styles/color-white-transparent})

(def transaction-fee-block-wrapper
  {:flex-direction :row
   :margin-top     15})

(def transaction-fee-column-wrapper
  {:flex              0.5
   :margin-horizontal 15})

(def transaction-fee-bubble
  (merge advanced-options-wrapper
         {:flex-direction     :row
          :justify-content    :space-between
          :padding-horizontal 15}))

(def transaction-fee-bubble-read-only
  (merge transaction-fee-bubble
         {:background-color styles/color-blue6}))

(def transaction-fee-info
  {:margin 15})

(def transaction-fee-input
  {:flex 1
   :keyboard-type          :numeric
   :auto-capitalize        "none"
   :placeholder            "0.000"
   :placeholder-text-color styles/color-white-transparent
   :selection-color        :white
   :style                  wallet.components.styles/text-input})