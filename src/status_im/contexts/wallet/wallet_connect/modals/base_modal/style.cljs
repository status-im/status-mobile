(ns status-im.contexts.wallet.wallet-connect.modals.base-modal.style
  (:require [status-im.constants :as constants]))

(defn container
  [bottom]
  {:position    :absolute
   :bottom      bottom
   :top         0
   :left        0
   :right       0
   :padding-top constants/sheet-screen-handle-height})

(def content-container
  {:padding-horizontal 20})

(def data-content-container
  {:flex               1
   :padding-horizontal 20})

(def data-items-container
  {:flex-direction :row
   :padding-top    12
   :padding-bottom 4
   :gap            16})

(def data-item
  {:flex             1
   :background-color :transparent})

(def auth-container
  {:height          48
   :margin-vertical 12})

(def warning-container
  {:align-items   :center
   :margin-bottom 12})

(def header-container
  {:padding-vertical 12})

(def header-dapp-name
  {:margin-top -4})
