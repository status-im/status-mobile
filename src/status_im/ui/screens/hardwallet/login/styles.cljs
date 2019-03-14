(ns status-im.ui.screens.hardwallet.login.styles
  (:require [status-im.ui.components.colors :as colors]))

(def container
  {:flex             1
   :background-color colors/white})

(def inner-container
  {:flex-direction  :column
   :flex            1
   :align-items     :center
   :justify-content :space-between})

(def login-view
  {:flex              1
   :margin-horizontal 16})

(def login-badge-container
  {:margin-top 24})

(def processing-view
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def sign-you-in
  {:margin-top 16
   :font-size  13})

(def bottom-button-container
  {:flex-direction    :row
   :margin-horizontal 12
   :margin-vertical   15
   :align-items       :center})

(def login-badge
  {:align-items :center})

(def login-badge-image-size 56)

(def login-badge-name
  {:margin-top 8})
