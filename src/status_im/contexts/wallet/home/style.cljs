(ns status-im.contexts.wallet.home.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.shell.constants :as constants]))

(def tabs
  {:padding-horizontal 20
   :padding-top        8
   :padding-bottom     12})

(def list-container
  {:padding-bottom constants/floating-shell-button-height})

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

(defn header-container
  [theme]
  {:background-color (colors/theme-colors colors/white colors/neutral-95 theme)})
