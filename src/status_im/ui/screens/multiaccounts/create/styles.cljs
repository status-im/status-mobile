(ns status-im.ui.screens.multiaccounts.create.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def create-multiaccount-view
  {:flex 1})

(def multiaccount-creating-view
  {:flex 1})

(def multiaccount-creating-indicatior
  {:flex            1
   :align-items     :center
   :justify-content :center
   :margin-bottom   100})

(def multiaccount-creating-text
  {:font-size   14
   :text-align  :center
   :margin-top  16})

(def logo-container
  {:margin-top  16
   :align-items :center})

(def logo
  {:size      82
   :icon-size 34})

(defstyle input-container
  {:margin-horizontal 16
   :margin-top        16})

(def input-description
  {:font-size   14
   :color       colors/gray})

(def bottom-container
  {:flex-direction    :row
   :margin-horizontal 12
   :margin-vertical   15})
