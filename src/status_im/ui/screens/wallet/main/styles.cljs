(ns status-im.ui.screens.wallet.main.styles
  (:require [status-im.components.styles :as styles]
            [status-im.utils.platform :as platform]))

(def wallet-container
  {:flex             1
   :background-color styles/color-white})

(def error-container
  {:align-self       :center
   :justify-content  :center
   :border-radius    20
   :flex-direction   :row
   :background-color styles/color-blue5})

(def error-message
  {:color         styles/color-white
   :padding-top   3
   :padding-right 10
   :font-size     13})

(def toolbar
  {:background-color styles/color-blue5
   :elevation        0})

(def toolbar-title-container
  {:flex           1
   :flex-direction :row
   :margin-left    6})

(def toolbar-title-inner-container
  {:flex-direction :row})

(def toolbar-title-text
  {:color        styles/color-white
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
   :padding         12})

;;;;;;;;;;;;;;;;;;
;; Main section ;;
;;;;;;;;;;;;;;;;;;

(def main-section
  {:padding          16
   :background-color styles/color-blue4})

(def total-balance-container
  {:margin-top      18
   :align-items     :center
   :justify-content :center})

(def total-balance
  {:flex-direction :row})

(def total-balance-value
  {:font-size 37
   :color     styles/color-white})

(def total-balance-currency
  {:font-size   37
   :margin-left 9
   :color       styles/color-white
   :opacity     0.4})

(def value-variation
  {:flex-direction :row
   :align-items    :center})

(def value-variation-title
  {:font-size 14
   :color     styles/color-white
   :opacity   0.6})

(def today-variation-container
  {:border-radius      100
   :margin-left        8
   :padding-horizontal 8
   :padding-vertical   4})

(def today-variation-container-positive
  (merge today-variation-container
         {:background-color styles/color-green-1}))

(def today-variation-container-negative
  (merge today-variation-container
         {:background-color styles/color-red-3}))

(def today-variation
  {:font-size 12})

(def today-variation-positive
  (merge today-variation
         {:color styles/color-green-2}))

(def today-variation-negative
  (merge today-variation
         {:color styles/color-red-4}))

(def buttons
  {:margin-top 34})

;;;;;;;;;;;;;;;;;;;;
;; Assets section ;;
;;;;;;;;;;;;;;;;;;;;

(def asset-section
  {:background-color styles/color-white
   :padding-vertical 16})

(def asset-section-title
  {:font-size   14
   :margin-left 16
   :color       styles/color-gray4})

(def asset-item-value-container
  {:flex           1
   :flex-direction :row
   :align-items    :center})

(def asset-item-value
  {:font-size 16
   :color     styles/color-black})

(def add-asset-icon
  {:border-radius    32
   :background-color styles/color-blue4-transparent})

(def add-asset-text
  {:font-size 16
   :color     styles/color-blue4})

(def asset-item-currency
  {:font-size   16
   :color       styles/color-gray4
   :margin-left 6})

(defn asset-border [color]
  {:border-color color :border-width 1 :border-radius 32})

(def corner-dot
  {:position         :absolute
   :top              12
   :right            6
   :width            4
   :height           4
   :border-radius    2
   :background-color styles/color-cyan})