(ns status-im.ui.screens.bootnodes-settings.styles
  (:require [status-im.ui.components.colors :as colors])
  (:require-macros [status-im.utils.styles :refer [defstyle]]))

(def wrapper
  {:flex             1
   :background-color :white})

(def bootnode-item-inner
  {:padding-horizontal 16})

(defstyle bootnode-item
  {:flex-direction     :row
   :background-color   :white
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(defstyle bootnode-item-name-text
  {:color   colors/black
   :ios     {:font-size      17
             :letter-spacing -0.2
             :line-height    20}
   :android {:font-size 16}})

(defstyle switch-container
  {:height           50
   :background-color colors/white
   :padding-left     15})
