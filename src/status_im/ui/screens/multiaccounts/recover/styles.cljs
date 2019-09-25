(ns status-im.ui.screens.multiaccounts.recover.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.styles :as styles]))

(def screen-container
  {:flex 1})

(styles/def inputs-container
  {:margin  16
   :desktop {:padding-top 15}})

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
   :font-size      13})

(def recover-release-warning
  {:margin-top     16
   :font-size      12
   :color          colors/gray})

(def recovery-phrase-input
  {:flex                1
   :text-align-vertical :top})
