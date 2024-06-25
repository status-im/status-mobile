(ns status-im.contexts.wallet.wallet-connect.modals.common.style
  (:require [status-im.constants :as constants]))

(defn container
  [bottom]
  {:position    :absolute
   :bottom      bottom
   :top         0
   :left        0
   :right       0
   :padding-top constants/sheet-screen-handle-height})

(def data-content-container
  {:flex               1
   :padding-horizontal 20})

(def data-item
  {:flex             1
   :background-color :transparent})
