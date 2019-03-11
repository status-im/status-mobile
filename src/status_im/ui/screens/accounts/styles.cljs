(ns status-im.ui.screens.accounts.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.styles :as common]
            [status-im.ui.components.colors :as colors]))

(def accounts-view
  {:flex 1})

(def accounts-container
  {:flex               1
   :padding-horizontal 16
   :background-color   colors/gray-lighter})

(def bottom-actions-container
  {:margin-bottom 16})

(def account-image-size 40)

(def account-title-text
  {:color     :black
   :font-size 17})

(defstyle accounts-list-container
  {:flex          1
   :margin-top    16
   :margin-bottom 16})

(defstyle account-view
  {:background-color   :white
   :flex-direction     :row
   :align-items        :center
   :padding-horizontal 16
   :height             64
   :border-radius      8})

(def account-badge-text-view
  {:margin-left  16
   :margin-right 31
   :flex-shrink  1})

(def account-badge-text
  {:font-size 17
   :color     colors/black})

(def account-badge-pub-key-text
  {:font-size  14
   :color      colors/gray
   :margin-top 4})

(def bottom-button-container
  {:margin-top    14
   :margin-bottom 6})
