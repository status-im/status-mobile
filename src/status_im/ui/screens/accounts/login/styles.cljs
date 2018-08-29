(ns status-im.ui.screens.accounts.login.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.styles :as st]
            [status-im.ui.components.colors :as colors]))

(defstyle login-view
  {:flex              1
   :margin-horizontal 16})

(defstyle login-badge-container
  {:margin-top 24})

(def processing-view
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def sign-you-in
  {:margin-top     16
   :font-size      13
   :color          colors/text-light-gray})

(def bottom-button-container
  {:flex-direction    :row
   :margin-horizontal 12
   :margin-vertical   15
   :align-items       :center})

(def login-badge
  {:align-items :center})

(def login-badge-image-size 56)

(def login-badge-name
  {:font-size  15
   :color      colors/text-light-gray
   :margin-top 8})

(def password-container
  {:margin-top 24})

(def save-password-checkbox-container
  {:margin-top 0
   :flex-direction :row
   :align-items :center})

(def save-password-unavailable
  {:margin-top 8
   :width "100%"
   :text-align :center
   :flex-direction :row
   :align-items :center})
