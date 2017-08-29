(ns status-im.ui.screens.wallet.main.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.components.styles :as common]
            [status-im.utils.platform :as platform]))

(def wallet-container
  {:flex             1
   :background-color common/color-white})

(def toolbar
  {:background-color common/color-blue5
   :elevation        0})

(def toolbar-title-container
  {:flex           1
   :flex-direction :row
   :margin-left    6})

(def toolbar-title-inner-container
  {:flex-direction :row})

(def toolbar-title-text
  {:color        common/color-white
   :font-size    17
   :margin-right 4})

(def toolbar-icon
  {:width  24
   :height 24})

(def toolbar-title-icon
  (merge toolbar-icon {:opacity 0.4}))

(def toolbar-buttons-container
  {:flex-direction  :row
   :flex-shrink     1
   :justify-content :space-between
   :width           68
   :margin-right    12})

;;;;;;;;;;;;;;;;;;
;; Main section ;;
;;;;;;;;;;;;;;;;;;

(def main-section
  {:padding          16
   :background-color common/color-blue4})

(def total-balance-container
  {:margin-top      18
   :align-items     :center
   :justify-content :center})

(def total-balance
  {:flex-direction :row})

(def total-balance-value
  {:font-size 37
   :color     common/color-white})

(def total-balance-currency
  {:font-size   37
   :margin-left 9
   :color       common/color-white
   :opacity     0.4})

(def value-variation
  {:flex-direction :row
   :align-items    :center})

(def value-variation-title
  {:font-size 14
   :color     common/color-white
   :opacity   0.6})

(def today-variation-container
  {:border-radius      4
   :margin-left        8
   :padding-horizontal 8
   :padding-vertical   4
   :background-color   common/color-green-1})

(def today-variation
  {:font-size 12
   :color     common/color-green-2})

(def buttons
  {:margin-top 34})

;;;;;;;;;;;;;;;;;;;;
;; Assets section ;;
;;;;;;;;;;;;;;;;;;;;

(def asset-section
  {:background-color common/color-white
   :padding-vertical 16})

(def asset-section-title
  {:font-size   14
   :margin-left 16
   :color       common/color-gray4})

(def asset-item-value-container
  {:flex           1
   :flex-direction :row
   :align-items    :center})

(def asset-item-value
  {:font-size 16
   :color     common/color-black})

(def asset-item-currency
  {:font-size   16
   :color       common/color-gray4
   :margin-left 6})
