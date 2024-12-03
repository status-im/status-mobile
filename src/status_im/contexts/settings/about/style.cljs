(ns status-im.contexts.settings.about.style
  (:require [react-native.safe-area :as safe-area]))

(defn list-content []
  {:padding-top (+ (safe-area/get-top) 56 56)})

(def category-spacing {:padding-bottom 12})

(def app-info-container {:padding-horizontal 20
                         :padding-top        8
                         :padding-bottom     16
                         :row-gap            16})
