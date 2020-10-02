(ns status-im.ui.screens.profile.user.styles
  (:require [status-im.ui.components.colors :as colors]))

(def share-link-button
  {:margin-top        12
   :margin-horizontal 16
   :margin-bottom     16})

(defn descr-container []
  {:border-width               1
   :border-color               colors/gray-lighter
   :border-top-right-radius    16
   :border-bottom-left-radius  16
   :border-top-left-radius     16
   :border-bottom-right-radius 4
   :padding-horizontal         12
   :padding-vertical           6})