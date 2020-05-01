(ns status-im.ui.screens.browser.open-dapp.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as components.styles]
            [status-im.utils.styles :as styles]))

(styles/defn input []
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

(defn browser-icon-container []
  {:width            40
   :height           40
   :border-radius    20
   :background-color colors/gray-lighter
   :align-items      :center
   :justify-content  :center})

(defn dapp-store-container []
  {:margin             16
   :border-color       colors/gray-lighter
   :margin-top         8
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
   :shadow-color       (if (colors/dark?)
                         "rgba(0, 0, 0, 0.75)"
                         "rgba(0, 12, 63, 0.2)")
   :elevation          2})
