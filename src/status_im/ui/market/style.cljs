(ns status-im.ui.market.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.shell.constants :as constants]))

(defn header-background
  [theme]
  (colors/theme-colors colors/white colors/neutral-95 theme))

(defn swap-header-container
  [theme]
  {:background-color   (header-background theme)
   :padding-horizontal 20
   :padding-vertical   12
   :flex-direction     :row
   :justify-content    :space-between})

(defn sort-header-container
  [theme]
  {:background-color   (header-background theme)
   :padding-horizontal 20
   :padding-vertical   12
   :align-items        :center
   :flex-direction     :row})

(defn market-header-text
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})

(defn sort-text
  [theme]
  {:color (colors/theme-colors colors/neutral-40 colors/neutral-40 theme)})

(def list-container
  {:padding-bottom constants/floating-shell-button-height})

(defn home-container
  []
  {:margin-top safe-area/top
   :flex       1})
