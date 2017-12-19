(ns status-im.ui.screens.wallet.wallet-list.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.styles :as st]
            [status-im.utils.platform :as platform]))

(def screen-container
  {:flex             1
   :background-color st/color-white})

(def wallet-colors
  {:blue-1 "#4360df"
   :gray-1 "#3c3d4e"})

(def toolbar
  {:elevation           0
   :border-bottom-width 1
   :border-color        st/color-light-gray2})

(def toolbar-icon
  {:width  24
   :height 24})

;;;;;;;;;;;;;;;;;
;; Wallet list ;;
;;;;;;;;;;;;;;;;;

(def wallet-list-screen
  {:flex           1
   :padding        16
   :padding-bottom 0})

(def wallet-list
  {:padding-bottom 16})

(def wallet-list-title
  {:font-size     14
   :margin-bottom 15
   :color         st/color-gray4})

;;;;;;;;;;;;;;;
;; List item ;;
;;;;;;;;;;;;;;;

(defstyle wallet-item
  {:flex             1
   :flex-direction   :row
   :align-items      :center
   :padding          16
   :margin-bottom    12
   :android          {:border-radius 4
                      :padding-right 12}
   :ios              {:border-radius 8
                      :padding-right 8}})

(def active-wallet-item
  {:background-color (get wallet-colors :blue-1)})

(def wallet-info
  {:flex-grow 1})

(def wallet-name
  {:font-size 14
   :color     st/color-white})

(def total-balance
  {:margin-top     5
   :margin-bottom  5
   :flex-direction :row})

(def total-balance-value
  {:font-size 26
   :color     st/color-white})

(def total-balance-currency
  (merge total-balance-value {:margin-left 6
                              :opacity     0.4}))

(def asset-list
  {:font-size 14
   :color     st/color-white
   :opacity   0.6})

(def select-wallet-icon
  {:height 24
   :width  24})
