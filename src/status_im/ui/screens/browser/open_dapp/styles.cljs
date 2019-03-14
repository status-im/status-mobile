(ns status-im.ui.screens.browser.open-dapp.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as styles]))

(def input-container
  {:flex-direction    :row
   :align-items       :center
   :border-radius     styles/border-radius
   :height            36
   :background-color  colors/gray-lighter
   :margin-horizontal 16
   :margin-bottom     9
   :margin-top        24})

(defstyle input
  {:flex               1
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
