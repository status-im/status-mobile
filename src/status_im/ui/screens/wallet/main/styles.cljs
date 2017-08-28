(ns status-im.ui.screens.wallet.main.styles
  (:require [status-im.components.styles :as st]
            [status-im.utils.platform :as platform]))

(def wallet-container
  {:flex             1
   :background-color st/color-white})

(def wallet-error-container
  {:align-self       :center
   :justify-content  :center
   :border-radius    20
   :flex-direction   :row
   :background-color st/color-blue5})

(def wallet-exclamation-container
  {:background-color st/color-red-2
   :justify-content  :center
   :margin-top       5
   :margin-left      10
   :margin-right     7
   :margin-bottom    5
   :border-radius    100})

(def wallet-error-exclamation
  {:width  16
   :height 16})

(def wallet-error-message
  {:color         st/color-white
   :padding-top   3
   :padding-right 10
   :font-size     13})

(def toolbar
  {:background-color st/color-blue5
   :elevation        0})

(def toolbar-title-container
  {:flex           1
   :flex-direction :row
   :margin-left    6})

(def toolbar-title-inner-container
  {:flex-direction :row})

(def toolbar-title-text
  {:color        st/color-white
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
   :background-color st/color-blue4})

(def total-balance-container
  {:margin-top      18
   :align-items     :center
   :justify-content :center})

(def total-balance
  {:flex-direction :row})

(def total-balance-value
  {:font-size 37
   :color     st/color-white})

(def total-balance-currency
  {:font-size   37
   :margin-left 9
   :color       st/color-white
   :opacity     0.4})

(def value-variation
  {:flex-direction :row
   :align-items    :center})

(def value-variation-title
  {:font-size 14
   :color     st/color-white
   :opacity   0.6})

(def today-variation-container
  {:border-radius      100
   :margin-left        8
   :padding-horizontal 8
   :padding-vertical   4})

(def today-variation-container-positive
  (merge today-variation-container
         {:background-color st/color-green-1}))

(def today-variation-container-negative
  (merge today-variation-container
         {:background-color st/color-red-3}))

(def today-variation
  {:font-size 12})

(def today-variation-positive
  (merge today-variation
         {:color st/color-green-2}))

(def today-variation-negative
  (merge today-variation
         {:color st/color-red-4}))

(def buttons
  {:margin-top 34})

;;;;;;;;;;;;;;;;;;;;
;; Assets section ;;
;;;;;;;;;;;;;;;;;;;;

(def asset-section
  {:background-color st/color-white
   :padding-vertical 16})

(def asset-section-title
  {:font-size   14
   :margin-left 16
   :color       st/color-gray4})

(def asset-item-value-container
  {:flex           1
   :flex-direction :row
   :align-items    :center})

(def asset-item-value
  {:font-size 16
   :color     st/color-black})

(def add-asset-icon
  {:border-radius    32
   :background-color st/color-blue4-transparent})

(def add-asset-text
  {:font-size 16
   :color     st/color-blue4})

(def asset-item-currency
  {:font-size   16
   :color       st/color-gray4
   :margin-left 6})

(defn asset-border [color]
  {:border-color color :border-width 1 :border-radius 32})