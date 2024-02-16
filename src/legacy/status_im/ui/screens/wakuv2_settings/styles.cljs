(ns legacy.status-im.ui.screens.wakuv2-settings.styles
  (:require
    [legacy.status-im.utils.styles :as styles]))

(def wrapper
  {:flex 1})

(def node-item-inner
  {:padding-horizontal 16})

(styles/def node-item
  {:flex-direction     :row
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(def node-item-name-text
  {:font-size 17})

(def switch-container
  {:height       50
   :padding-left 15})
