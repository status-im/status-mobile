(ns status-im.ui.screens.currency-settings.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def wrapper
  {:flex             1
   :background-color :white})

(defstyle currency-item
  {:flex-direction     :row
   :justify-content    :space-between
   :background-color   :white
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(def currency-name-text
  {:color       colors/black
   :font-size   17
   :line-height 20})
