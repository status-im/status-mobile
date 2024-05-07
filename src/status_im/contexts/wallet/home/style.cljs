(ns status-im.contexts.wallet.home.style
  (:require
    [react-native.safe-area :as safe-area]))

(def tabs
  {:padding-horizontal 20
   :padding-top        8
   :padding-bottom     12})

(def accounts-list
  {:padding-top    8
   :padding-bottom 16
   :flex-grow      0})

(def accounts-list-container
  {:padding-horizontal 20})

(def separator
  {:width 12})

(defn home-container
  []
  {:margin-top (+ (safe-area/get-top) 8)
   :flex       1})
