(ns status-im.ui.screens.wallet.onboarding.setup.styles
  (:require [status-im.ui.components.colors :as colors]))

(def setup-image-container
  {:align-items :center
   :margin      41})

(def setup-image
  {:width  151
   :height 77})

(def signing-phrase
  {:background-color colors/white
   :border-radius    8
   :margin-left      16
   :margin-right     16
   :flex-direction   :row})

(def signing-word
  {:flex            1
   :height          52
   :align-items     :center
   :justify-content :center})

(def signing-word-text
  {:font-size      15
   :letter-spacing -0.2})

(def description
  {:font-size      14
   :letter-spacing -0.2
   :color          colors/white
   :margin-left    24
   :margin-right   24
   :margin-top     16
   :text-align     :center})

(def bottom-buttons
  {:background-color colors/blue
   :padding-vertical 8})

(def got-it-button-text
  {:padding-horizontal 0})