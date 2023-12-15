(ns status-im2.contexts.wallet.home.style
  (:require
    [react-native.safe-area :as safe-area]))

(def tabs
  {:padding-horizontal 20
   :padding-top        8
   :padding-bottom     12})

(def accounts-container
  {:height 112})

(def overview-container
  {:height 86})

(def accounts-list
  {:padding-top    32
   :padding-bottom 12
   :max-height     112})

(def accounts-list-container
  {:padding-horizontal 20})

(def empty-container-style
  {:justify-content :center
   :flex            1
   :margin-bottom   44})

(def separator
  {:width 12})

(def selected-tab-container
  {:padding-horizontal 8})

(def home-container
  {:margin-top (safe-area/get-top)
   :flex       1})
