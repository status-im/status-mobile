(ns legacy.status-im.ui.screens.browser.empty-tab.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]))

(def input
  {:height  36
   :padding 0})

(def input-container-style
  {:margin-horizontal 16
   :margin-vertical   10})

(defn browser-icon-container
  []
  {:width            40
   :height           40
   :border-radius    20
   :background-color colors/gray-lighter
   :align-items      :center
   :justify-content  :center})

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

(defn dapps-account
  [color]
  {:flex-direction     :row
   :background-color   color
   :border-radius      36
   :padding-horizontal 8
   :padding-vertical   6
   :align-items        :center
   :justify-content    :center
   :shadow-offset      {:width 0 :height 1}
   :shadow-radius      6
   :shadow-opacity     1
   :shadow-color       (if (colors/dark?)
                         "rgba(0, 0, 0, 0.75)"
                         "rgba(0, 12, 63, 0.2)")
   :elevation          2})
