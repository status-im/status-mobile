(ns status-im.ui.screens.status.styles
  (:require [status-im.ui.components.colors :as colors]))

(defn descr-container []
  {:border-width               1
   :border-color               colors/gray-lighter
   :border-top-right-radius    16
   :border-bottom-left-radius  16
   :border-top-left-radius     16
   :border-bottom-right-radius 4
   :padding-horizontal         12
   :padding-vertical           6})