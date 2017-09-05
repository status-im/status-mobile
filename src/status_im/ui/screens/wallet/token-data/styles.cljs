(ns status-im.ui.screens.wallet.token-data.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle]])
  (:require [status-im.components.styles :as common]
            [status-im.utils.platform :as platform]))

(def screen-container
  {:flex             1
   :background-color common/color-white})

(def tabs-container
  {:padding 16})

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Token balance section ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def total-balance-container
  {:margin-top 10
   :align-items :center
   :justify-content :center})

(def total-balance
  {:flex-direction :row})

(def total-balance-value
  {:font-size 36
   :color common/color-black})

(def total-balance-currency
  {:font-size 36
   :margin-left 6
   :color common/color-gray7})

(def value-variation
  {:flex-direction :row
   :align-items :center})

(def fiat-value
  {:font-size 14
   :color common/color-gray7})

(def today-variation-container
  {:border-radius 10
   :margin-left 5
   :padding-horizontal 8
   :padding-vertical 2
   :background-color common/color-green-1})

(def today-variation
  {:font-size 14
   :color common/color-green-2})

;;;;;;;;;;;;;;;;;;;;
;; Action buttons ;;
;;;;;;;;;;;;;;;;;;;;

(def action-buttons-container
  {:flex-direction :row
   :background-color common/color-light-blue2
   :margin-top   24
   :border-radius 7})

(def action-button
  {:padding-horizontal 26
   :padding-vertical   12})

(def action-button-left
  (merge action-button
         {:border-right-width 1
          :border-color common/color-separator}))

(def action-button-text
  {:font-size 16
   :color common/color-blue4})

;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Transactions section ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(def transaction-section
  {:background-color common/color-white
   :margin-top 25})

(def transaction-section-title
  {:font-size 14
   :color common/color-gray4})

(def transaction-list {})

(def transaction-item-container
  {:flex-direction :row
   :align-items      :center
   :padding-vertical 10})

(def transaction-item-icon-container
  {:border-radius 30
   :width 40
   :height 40
   :background-color common/color-light-blue4
   :align-items :center
   :justify-content :center
   :margin-right 16})

(def transaction-item-icon
  {:height 26
   :width 26})

(def transaction-item-info-container
  {:flex 1})

(def transaction-item-value
  {:font-size 18
   :color common/color-black})

(def transaction-item-recipient-container
  {:flex 1
   :flex-direction :row})

(def transaction-item-recipient-label
  {:font-size 14
   :color common/color-black})

(def transaction-item-recipient
  {:font-size 14
   :color common/color-gray4
   :margin-left 4})

(def transaction-item-details-icon
  {:flex-shrink 1
   :height 20
   :width 20})

(def transaction-list-separator
  {:margin-left 70
   :border-bottom-width 1
   :border-color common/color-separator})

;;;;;;;;;;;;;;;;;
;; Market data ;;
;;;;;;;;;;;;;;;;;

(def market-data-row
  {:flex-direction :row
   :justify-content :space-between})

(def market-data-item-container
  {:margin-top 8
   :margin-bottom 8})

(def market-data-item-title
  {:font-size 16
   :color common/color-gray4
   :margin-bottom 6})

(defn market-data-item-text [large?]
  {:font-size (if large? 18 16)
   :color common/color-black})

(def market-data-links-container
  {:flex-direction :row})

(def market-data-link
  {:border-radius 6
   :background-color common/color-light-blue4
   :padding-vertical 4
   :padding-horizontal 6
   :margin-right 12
   :flex-direction :row
   :align-items :center})

(def market-data-link-title
  {:font-size 16
   :color common/color-blue4})

(def market-data-link-icon
  {:margin-left 5
   :width 26
   :height 26})
