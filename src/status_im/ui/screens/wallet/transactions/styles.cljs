(ns status-im.ui.screens.wallet.transactions.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.main-tabs.styles :as tabs.styles]))

(defnstyle tab [active?]
  {:flex                1
   :height              tabs.styles/tab-height
   :justify-content     :center
   :align-items         :center
   :padding-bottom      (if active? 0 1)
   :border-bottom-width (if active? 2 1)
   :border-bottom-color (if active?
                          colors/blue
                          colors/gray-transparent)})

(def tabs-container
  {:flex-direction :row
   :height         tabs.styles/tab-height})

(defnstyle tab-title [active?]
  {:ios        {:font-size 15}
   :android    {:font-size 14}
   :text-align :center
   :color      (if active?
                 colors/blue
                 colors/black)})

(def transactions
  {:flex             1
   :background-color :white})

(def tab-unsigned-transactions-count
  (merge (tab-title false)
         {:color colors/gray}))

(def forward
  {:color colors/gray})

(def empty-text
  {:text-align        :center
   :margin-top        22
   :margin-horizontal 92})

(defstyle amount-time
  {:flex-direction  :row
   :justify-content :space-between
   :padding-right   22
   :padding-left    17
   :ios             {:padding-top 13}
   :android         {:padding-top 14}})

(def tx-amount
  {:flex-grow    1
   :flex-shrink  1
   :margin-right 10
   :font-size    17})

(def tx-time
  {:flex-grow   1
   :font-size   14
   :text-align  :right
   :color       colors/blue})

(def address-row
  {:flex-direction :row
   :padding-right  22
   :padding-left   17
   :padding-top    4})

(def address-item
  {:font-size 16
   :color     colors/gray})

(def address-label
  (merge address-item
         {:margin-right 5}))

(def address-contact
  (merge address-item
         {:color        colors/black
          :margin-right 5}))

(def address-hash
  (merge address-item
         {:flex-shrink 1}))

(def action-buttons
  {:flex             1
   :flex-direction   :row
   :padding-vertical 12})

(def sign-all-view
  {:flex              1
   :flex-direction    :column
   :justify-content   :center
   :background-color  colors/gray-transparent})

(def sign-all-popup
  {:align-self        :flex-start
   :background-color  colors/white
   :margin-horizontal 12
   :border-radius     8})

(def sign-all-popup-text
  {:margin-top        8
   :margin-horizontal 12})

(def sign-all-actions
  {:flex-direction    :row
   :justify-content   :space-between
   :margin-horizontal 12
   :margin-vertical   20})

(def sign-all-input
  {:width  150
   :height 38})

(def sign-all-done
  {:position :absolute
   :right    0
   :top      0})

(def sign-all-done-button
  {:background-color :transparent})

(defn transaction-icon-background [color]
  {:justify-content  :center
   :align-items      :center
   :width            40
   :height           40
   :border-radius    32
   :background-color color})

;; transaction details

(def details-row
  {:flex-direction  :row
   :margin-vertical 5})

(def details-item-label
  {:flex         1
   :margin-right 10
   :color        colors/gray
   :font-size    14})

(def details-item-value-wrapper
  {:flex 5})

(def details-item-value
  {:font-size 14
   :color     colors/black})

(def details-item-extra-value
  {:font-size 14
   :color     colors/gray})

(def details-header
  {:margin-horizontal 16
   :margin-top        10
   :flex-direction    :row})

(def details-header-icon
  {:margin-vertical 7})

(def details-header-infos
  {:flex            1
   :flex-direction  :column
   :margin-left     12
   :margin-vertical 7})

(def details-header-value
  {:font-size 16
   :color     colors/black})

(def details-header-date
  {:font-size 14
   :color     colors/gray})

(def details-block
  {:margin-horizontal 16})

(def progress-bar
  {:flex-direction  :row
   :margin-vertical 10
   :height          2})

(defn progress-bar-done [done]
  {:flex             done
   :background-color colors/blue})

(defn progress-bar-todo [todo]
  {:flex             todo
   :background-color colors/blue
   :opacity          0.30})

(def details-confirmations-count
  {:color           colors/black
   :font-size       15
   :margin-vertical 2})

(def details-confirmations-helper-text
  {:color           colors/gray
   :font-size       14
   :margin-vertical 2})

(def details-separator
  {:background-color colors/gray-light
   :height           1
   :margin-vertical  10})

(def corner-dot
  {:position         :absolute
   :top              0
   :right            0
   :width            4
   :height           4
   :border-radius    2
   :background-color colors/cyan})

(def filter-container
  {:flex 1})

(def transactions-view
  {:flex             1
   :background-color :white})
