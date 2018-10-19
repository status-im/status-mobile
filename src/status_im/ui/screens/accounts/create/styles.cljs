(ns status-im.ui.screens.accounts.create.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def create-account-view
  {:flex               1
   :background-color colors/white})

(def account-creating-view
  {:flex               1
   :padding-horizontal 14})

(def account-creating-logo-container
  {:margin-top  37
   :align-items :center})

(def account-creating-logo
  {:size      82
   :icon-size 34})

(def account-creating-indicatior
  {:flex            1
   :align-items     :center
   :justify-content :center
   :margin-bottom   100})

(def account-creating-text
  {:font-size      14
   :line-height    21
   :letter-spacing -0.2
   :text-align     :center
   :color          colors/black
   :margin-top     16})

(def logo-container
  {:position    :absolute
   :top         37
   :left        0
   :right       0
   :align-items :center})

(def logo
  {:size      82
   :icon-size 34})

(defstyle input-container
  {:margin-horizontal 16
   :margin-top        105
   :android           {:padding-top 13
                       :margin-top  92}})

(def input-description
  {:font-size      14
   :letter-spacing -0.2
   :color          colors/gray
   :line-height    21})

(def bottom-container
  {:flex-direction    :row
   :margin-horizontal 12
   :margin-vertical   15})