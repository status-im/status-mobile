(ns status-im.ui.components.tabbar.core
  (:require [status-im.utils.platform :as platform]))

(defn get-height
  []
  (if platform/android?
    56
    (if platform/iphone-x?
      84
      50)))
