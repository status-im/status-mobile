(ns status-im.contexts.wallet.wallet-connect.sign-message.style
  (:require [quo.foundations.colors :as colors]
            [status-im.constants :as constants]))

(defn container
  [bottom]
  {:position    :absolute
   :bottom      bottom
   :top         0
   :left        0
   :right       0
   :padding-top constants/sheet-screen-handle-height})

(def content-container
  {:flex               1
   :padding-horizontal 20})

(def fees-container
  {:padding-top      12
   :padding-bottom   4
   :background-color colors/white})

(def auth-container
  {:height          48
   :margin-vertical 12})

(def warning-container
  {:align-items   :center
   :margin-bottom 12})
