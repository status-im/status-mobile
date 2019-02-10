(ns status-im.ui.screens.extensions.add.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.components.colors :as colors]))

(def screen
  {:flex             1
   :background-color colors/white})

(def wrapper
  {:flex   1
   :margin 16})

(def input-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between
   :border-radius   styles/border-radius
   :height          52
   :margin-top      15})

(defstyle input
  {:flex           1
   :font-size      15
   :letter-spacing -0.2
   :android        {:padding 0}})

(def bottom-container
  {:flex-direction    :row
   :margin-horizontal 12
   :margin-vertical   15})

(def hooks
  {:margin-top  20
   :margin-left 10})

(def text
  {:color colors/black})

(def cartouche-container
  {:flex              1
   :margin-top        16
   :margin-horizontal 16})

(def cartouche-header
  {:color colors/gray})

(def cartouche-content-wrapper
  {:flex-direction   :row
   :margin-top       8
   :border-color     colors/gray-lighter
   :border-width     1
   :border-radius    styles/border-radius
   :padding          16
   :background-color colors/white-transparent})

(def qr-code
  {:margin-right 14})