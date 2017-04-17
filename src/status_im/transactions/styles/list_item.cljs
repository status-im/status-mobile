(ns status-im.transactions.styles.list-item
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.components.styles :as st]))

(def item
  {:padding-vertical   19
   :padding-horizontal 16
   :flex               1
   :flex-direction     :row
   :align-items        :center})

(def item-photo
  {:width            86
   :height           48
   :border-radius    100
   :background-color st/color-dark-blue-3
   :flex-direction   :row
   :align-items      :center})

(def item-info
  {:margin-left 16
   :flex        1})

(defstyle item-info-recipient
  {:color       st/color-light-blue
   :font-size   15
   :flex-shrink 1
   :android     {:margin-bottom 5}
   :ios         {:margin-bottom 4}})

(defstyle item-info-amount
  {:color   st/color-white
   :android {:font-size 19}
   :ios     {:font-size 20}})

(def item-deny-btn
  {:margin-left 16})

(def item-deny-btn-icon
  {:width  24
   :height 24})

(def photo-size 48)

(def photo-placeholder
  {:width        48
   :height       48})

(def item-photo-icon
  {:margin-left 4
   :width       24
   :height      24})
