(ns legacy.status-im.ui.screens.browser.empty-tab.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]))

(def input
  {:height  36
   :padding 0})

(def input-container-style
  {:margin-horizontal 16
   :margin-vertical   10})

(defn dapp-store-container
  []
  {:margin             16
   :border-color       colors/gray-lighter
   :margin-top         18
   :border-width       1
   :border-radius      12
   :padding-vertical   16
   :padding-horizontal 44
   :align-items        :center})

(def open-dapp-store
  {:margin-top  12
   :font-size   15
   :font-weight "500"
   :line-height 22})
