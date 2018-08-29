(ns status-im.ui.screens.accounts.recover.styles
  (:require [status-im.ui.components.colors :as colors]))

(def screen-container
  {:flex             1
   :background-color colors/white})

(def inputs-container
  {:margin 16})

(def password-input
  {:margin-top 10})

(def bottom-button-container
  {:flex-direction    :row
   :margin-horizontal 12
   :margin-vertical   15})

(def processing-view
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def sign-you-in
  {:margin-top     16
   :font-size      13
   :color          colors/text-light-gray})

(def recovery-phrase-input
  {:flex                1
   :text-align-vertical :top})
