(ns status-im.ui.screens.wallet.main.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

;; Main section

(defstyle main-section
  {:flex    1
   :android {:background-color colors/white}
   :ios     {:background-color colors/blue}})

(defstyle scroll-bottom
  {:background-color colors/white
   :zIndex           -1
   :position         :absolute
   :left             0
   :right            0
   :android          {:height 0}
   :ios              {:height 9999}})

(def section
  {:background-color colors/blue})

(def backup-seed-phrase-container
  {:flex-direction   :row
   :align-items      :center
   :border-radius    8
   :margin           16
   :background-color colors/black-transparent
   :padding-top      10
   :padding-bottom   10
   :padding-left     14
   :padding-right    12})

(def backup-seed-phrase-text-container
  {:flex 1})

(def backup-seed-phrase-title
  {:font-size   15
   :line-height 20
   :color       colors/white})

(def backup-seed-phrase-description
  {:line-height 20
   :color       colors/white-transparent})

(def total-balance-container
  {:align-items     :center
   :justify-content :center
   :padding-top     49
   :padding-bottom  38})

(def total-balance
  {:flex-direction :row})

(def total-balance-value
  {:font-size   32
   :font-weight :bold
   :color       colors/white})

(def total-balance-tilde
  {:color colors/white-transparent})

(defstyle total-balance-currency
  {:font-size   32
   :font-weight :bold
   :margin-left 6
   :color       colors/white-transparent})

(def snackbar-container
  {:background-color colors/gray})

(def snackbar-text
  {:color             colors/white
   :margin-horizontal 50
   :margin-vertical   10
   :text-align        :center})

;; Actions section

(def action-section
  {:background-color colors/blue})

(def action
  {:background-color colors/white-transparent
   :width            40
   :height           40
   :border-radius    50})

(def action-label
  {:color :white})

(def action-separator
  {:height           1
   :background-color colors/white-light-transparent
   :margin-left      70})

;; Assets section

(def asset-section
  {:flex             1
   :padding-top      5
   :padding-bottom   20
   :background-color colors/white})

(def asset-section-header
  {:font-size     15
   :color         colors/gray
   :margin-left   16
   :margin-top    7
   :margin-bottom 3})

(def asset-item-container
  {:flex            1
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def asset-item-value-container
  {:flex           1
   :flex-direction :row
   :align-items    :center})

(def asset-item-value
  {:flex      -1
   :font-size 16
   :color     colors/black})

(def asset-item-currency
  {:font-size   16
   :color       colors/gray
   :margin-left 6})

(def asset-item-price
  {:font-size   16
   :color       colors/gray
   :margin-left 6})

(def wallet-address
  {:color          :white
   :text-align     :center
   :font-size      15
   :letter-spacing -0.2
   :line-height    20})

(def address-section
  (merge
   section
   {:flex-grow   1
    :align-items :center
    :padding     20}))

(def modal-history
  {:flex             1
   :background-color :white})
