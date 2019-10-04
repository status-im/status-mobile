(ns status-im.ui.screens.browser.open-dapp.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as components.styles]
            [status-im.utils.styles :as styles]))

(styles/def input
  {:border-radius      components.styles/border-radius
   :background-color   colors/gray-lighter
   :margin-horizontal  16
   :margin-bottom      9
   :margin-top         24
   :height             36
   :padding-horizontal 14
   :desktop            {:height 30
                        :width  "100%"}
   :android            {:padding 0}})

(def browser-icon-container
  {:width            40
   :height           40
   :border-radius    20
   :background-color colors/gray-lighter
   :align-items      :center
   :justify-content  :center})

(def dapp-store-container
  {:margin             16
   :border-color       colors/gray-lighter
   :margin-top         8
   :border-width       1
   :border-radius      12
   :padding-vertical   16
   :padding-horizontal 44
   :align-items        :center})

(def privacy-container
  {:margin-horizontal 16
   :margin-top        8
   :border-color      colors/gray-lighter
   :border-width      1
   :border-radius     12
   :padding-vertical  8
   :padding-left      16})

(def open-dapp-store
  {:margin-top  12
   :font-size   15
   :font-weight "500"
   :line-height 22})

(def close-icon-container
  {:width            21
   :height           21
   :border-radius    12
   :background-color colors/gray
   :align-items      :center
   :justify-content  :center})

(def might-break
  {:margin-left 34
   :font-size   13
   :font-weight "500"
   :color       colors/gray
   :line-height 18
   :margin-top  2})

(defn dapps-account [color]
  {:flex-direction     :row
   :background-color   color
   :border-radius      36
   :padding-horizontal 8
   :padding-vertical   6
   :flex               1
   :align-items        :center
   :justify-content    :center
   :shadow-offset      {:width 0 :height 1}
   :shadow-radius      6
   :shadow-opacity     1
   :shadow-color       "rgba(0, 12, 63, 0.2)"
   :elevation          2})
