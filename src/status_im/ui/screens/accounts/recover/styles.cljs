(ns status-im.ui.screens.accounts.recover.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def screen-container
  {:flex             1
   :background-color colors/white})

(defstyle inputs-container
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
   :font-size      13
   :color          colors/black})

(def recover-release-warning
  {:margin-top     16
   :font-size      12
   :color          colors/gray})

(def recovery-phrase-input
  {:flex                1
   :text-align-vertical :top})
