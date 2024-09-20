(ns status-im.contexts.wallet.send.select-asset.style
  (:require [react-native.navigation :as navigation]
            [react-native.platform :as platform]))

(def container
  {:flex        1
   :padding-top (when platform/android? (navigation/status-bar-height))})

(def title-container
  {:margin-horizontal 20
   :margin-vertical   12})

(defn empty-container-style
  [margin-bottom]
  {:justify-content :center
   :flex            1
   :margin-bottom   margin-bottom})

(def search-input-container
  {:padding-horizontal 20
   :padding-vertical   8})
