(ns status-im.ui.screens.wallet.send.styles
  (:require [status-im.components.styles :as st]))

(def wallet-container
  {:flex             1
   :background-color st/color-white})

(def toolbar
  {:background-color st/color-blue5
   :elevation        0})

(def toolbar-title-container
  {:flex           1
   :flex-direction :row
   :margin-left    6})

(def toolbar-title-text
  {:color        st/color-white
   :font-size    17
   :margin-right 4})

(def toolbar-icon
  {:width  24
   :height 24})

(def toolbar-title-icon
  (merge toolbar-icon {:opacity 0.4}))

(def toolbar-buttons-container
  {:flex-direction  :row
   :flex-shrink     1
   :justify-content :space-between
   :width           68
   :margin-right    12})