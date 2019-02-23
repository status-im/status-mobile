(ns status-im.ui.screens.currency-settings.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def wrapper
  {:flex             1})

(defstyle currency-item
  {:flex-direction     :row
   :justify-content    :space-between
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(defstyle currency-name-text
  {:color   colors/black
   :ios     {:font-size      17
             :letter-spacing -0.2
             :line-height    20}
   :android {:font-size 16}})
