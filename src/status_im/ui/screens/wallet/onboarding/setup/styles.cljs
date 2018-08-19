(ns status-im.ui.screens.wallet.onboarding.setup.styles
  (:require [status-im.ui.components.colors :as colors]))

(def signing-phrase
  {:background-color  colors/white
   :border-radius     8
   :height            68
   :margin-horizontal 28
   :flex-direction    :row})

(def signing-emoji-container
  {:height          68
   :flex            1
   :align-items     :center
   :justify-content :center})

(def signing-emoji-container-left-border
  {:border-left-color colors/gray-border
   :border-left-width 1})

(def signing-emoji
  {:font-size      24
   :color          colors/black
   :letter-spacing -0.2})

(def super-safe-transactions
  {:margin-top  40
   :font-size   22
   :text-align  :center
   :color       colors/white
   :font-weight :bold})

(def description
  {:margin-top     12
   :font-size      14
   :font-style     :normal
   :letter-spacing -0.17
   :line-height    21
   :opacity        0.8
   :color          colors/white
   :text-align     :center})

(def warning-container
  {:flex-direction :row
   :margin-top     28})

(def info-icon
  {:color           colors/white
   :container-style {:background-color  colors/blue
                     :z-index           5
                     :position          :absolute
                     :top               0
                     :margin-left       -19
                     :padding-horizontal 7
                     :left               "50%"}})

(def warning
  {:flex               1
   :border-color       "rgba(255, 255, 255, 0.3)"
   :border-width       1
   :border-radius      8
   :font-size          14
   :font-style         :normal
   :letter-spacing     -0.17
   :line-height        21
   :margin-top         12
   :margin-horizontal  57
   :padding-vertical   18
   :padding-horizontal 24
   :opacity            0.8
   :color              colors/white
   :text-align         :center})

(def bottom-buttons
  {:background-color colors/blue
   :padding-vertical 8})

(def got-it-button-text
  {:padding-horizontal 0})

(def modal
  {:flex             1
   :background-color colors/blue})
