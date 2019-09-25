(ns status-im.ui.screens.bootnodes-settings.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.styles :as styles]))

(def wrapper
  {:flex             1
   :background-color :white})

(def bootnode-item-inner
  {:padding-horizontal 16})

(styles/def bootnode-item
  {:flex-direction     :row
   :background-color   :white
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(def bootnode-item-name-text
  {:font-size   17})

(def switch-container
  {:height           50
   :background-color colors/white
   :padding-left     15})
