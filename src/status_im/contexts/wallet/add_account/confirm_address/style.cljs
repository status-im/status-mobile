(ns status-im.contexts.wallet.add-account.confirm-address.style
  (:require [status-im.constants :as constants]))

(defn container
  [purpose]
  {:flex       1
   :margin-top (when (= purpose constants/add-address-to-save-type) -39)})

(def data-item
  {:margin-horizontal  20
   :padding-vertical   8
   :padding-horizontal 12})

(def save-address-drawer-bar-container
  {:position :absolute
   :left     0
   :right    0})
