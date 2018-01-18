(ns status-im.ui.screens.wallet.transactions.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as styles]
            [status-im.ui.screens.main-tabs.styles :as tabs.styles]))

(def error-container
  {:align-self       :center
   :justify-content  :center
   :border-radius    8
   :padding-vertical 4
   :flex-direction   :row
   :background-color styles/color-gray9})

(def error-message
  {:color         styles/color-black
   :padding-top   3
   :padding-right 10
   :font-size     13})

(defnstyle tab [active?]
  {:flex                1
   :height              tabs.styles/tab-height
   :justify-content     :center
   :align-items         :center
   :border-bottom-width (if active? 2 1)
   :border-bottom-color (if active?
                          styles/color-blue4
                          styles/color-gray10-transparent)})

(def tabs-container
  {:flexDirection :row
   :height        tabs.styles/tab-height})

(defnstyle tab-title [active?]
  {:ios        {:font-size 15}
   :android    {:font-size 14}
   :text-align :center
   :color      (if active?
                 styles/color-blue4
                 styles/color-black)})

(def tab-unsigned-transactions-count
  (merge (tab-title false)
         {:color styles/color-gray10}))

(def forward
  {:color styles/color-gray7})

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
   :color       styles/color-gray4})

(def address-row
  {:flex-direction :row
   :padding-right  22
   :padding-left   17
   :padding-top    4})

(def address-item
  {:font-size 16
   :color     styles/color-gray4})

(def address-label
  (merge address-item
         {:margin-right 5}))

(def address-contact
  (merge address-item
         {:color        styles/color-black
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
   :background-color  styles/color-gray-transparent})

(def sign-all-popup
  {:align-self        :flex-start
   :background-color  styles/color-white
   :margin-horizontal 12
   :border-radius     8})

(def sign-all-popup-sign-phrase
  {:border-radius     8
   :margin-top        12
   :margin-horizontal 12
   :text-align        :center
   :padding-vertical  9
   :background-color  styles/color-light-gray})

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
   :color        styles/color-gray4
   :font-size    14})

(def details-item-value-wrapper
  {:flex 5})

(def details-item-value
  {:font-size 14
   :color     styles/color-black})

(def details-item-extra-value
  {:font-size 14
   :color     styles/color-gray4})

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
   :color     styles/color-black})

(def details-header-date
  {:font-size 14
   :color     styles/color-gray4})

(def details-block
  {:margin-horizontal 16})

(def progress-bar
  {:flex-direction  :row
   :margin-vertical 10
   :height          2})

(defn progress-bar-done [done]
  {:flex             done
   :background-color styles/color-blue2})

(defn progress-bar-todo [todo]
  {:flex             todo
   :background-color styles/color-blue2
   :opacity          0.30})

(def details-confirmations-count
  {:color           styles/color-black
   :font-size       15
   :margin-vertical 2})

(def details-confirmations-helper-text
  {:color           styles/color-gray4
   :font-size       14
   :margin-vertical 2})

(def details-separator
  {:background-color styles/color-light-gray3
   :height           1
   :margin-vertical  10})

(def corner-dot
  {:position         :absolute
   :top              0
   :right            0
   :width            4
   :height           4
   :border-radius    2
   :background-color styles/color-cyan})

(def filter-container
  {:flex             1})
   ;:background-color colors/white})
