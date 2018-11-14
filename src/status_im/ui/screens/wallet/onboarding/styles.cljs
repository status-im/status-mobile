(ns status-im.ui.screens.wallet.onboarding.styles
  (:require [status-im.ui.components.colors :as colors]))

(def border-top-justify
  {:justify-content  :space-between
   :border-top-color colors/white-light-transparent
   :border-top-width 1
   :flex             1})

(def signing-phrase
  {:background-color colors/white
   :border-radius    8
   :flex-direction   :row})

(def signing-word
  {:flex              1
   :height            66
   :border-left-color "#ECECF0"
   :border-left-width 1
   :align-items       :center
   :justify-content   :center})

(def signing-word-text
  {:font-size      15
   :font-weight    "600"
   :color          colors/black
   :letter-spacing -0.2})

(def bottom-buttons
  {:background-color colors/blue
   :padding-vertical 8})

(def got-it-button-text
  {:padding-horizontal 0})

(def modal
  {:flex             1
   :background-color colors/blue})

(def bottom-button-container
  {:flex-direction   :row,
   :border-top-width 1
   :background-color colors/blue
   :border-top-color colors/white-light-transparent})

(def explanation-container
  {:margin-top   40
   :margin-left  2
   :margin-right 2
   :align-items  :center})

(def super-safe-text
  {:color         colors/white
   :margin-bottom 12
   :font-size     22
   :font-weight   :bold})

(def super-safe-explainer-text
  {:color         colors/white
   :text-align    :center
   :font-size     15
   :line-height   22
   :margin-bottom 30})

;; onboarding screen styles

(def root
  {:flex               1
   :background-color   colors/blue
   :align-items        :center
   :justify-content    :center
   :padding-horizontal 30})

(def onboarding-image-container
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def onboarding-image
  {:width  285
   :height 312})

(def onboarding-title
  {:line-height    28
   :font-size      22
   :font-weight    :bold
   :letter-spacing -0.3
   :text-align     :center
   :color          colors/white})

(def onboarding-text
  {:line-height    21
   :margin-top     8
   :margin-bottom  32
   :font-size      14
   :letter-spacing -0.2
   :text-align     :center
   :color          colors/white-transparent})

(def set-up-button
  {:flex-direction   :row
   :background-color (colors/alpha colors/black 0.1)
   :margin-bottom    32})

(def set-up-button-label
  {:color colors/white})
