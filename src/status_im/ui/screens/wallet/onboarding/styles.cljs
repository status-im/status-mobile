(ns status-im.ui.screens.wallet.onboarding.styles
  (:require [status-im.ui.components.colors :as colors]))

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
   :color          colors/white-lighter-transparent})

(def set-up-button
  {:flex-direction   :row
   :background-color (colors/alpha colors/black 0.1)
   :margin-bottom    32})

(def set-up-button-label
  {:color "white"})