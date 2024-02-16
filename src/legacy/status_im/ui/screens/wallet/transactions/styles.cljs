(ns legacy.status-im.ui.screens.wallet.transactions.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.utils.styles :as styles]))

(def forward
  {:color colors/gray})

(def empty-text
  {:text-align        :center
   :color             colors/gray
   :margin-top        22
   :margin-horizontal 92})

(styles/def amount-time
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
  {:flex-grow  1
   :font-size  14
   :text-align :right
   :color      colors/blue})

(def address-row
  {:flex-direction :row
   :padding-right  22
   :padding-left   17
   :padding-top    4})

(def address-label
  {:margin-right 5
   :font-size    16
   :color        colors/gray})

(def address-contact
  {:margin-right 5
   :font-size    16})

(def address-hash
  {:flex-shrink 1})

(defn transaction-icon-background
  [color]
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
   :color        colors/gray})

(def details-item-value-wrapper
  {:flex 5})

(def details-item-value
  {:font-size 14})

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
  {:font-size 16})

(def details-header-date
  {:font-size 14
   :color     colors/gray})

(def details-block
  {:margin-horizontal 16})

(def progress-bar
  {:flex-direction  :row
   :margin-vertical 10
   :height          2})

(defn progress-bar-done
  [done failed?]
  {:flex             done
   :background-color (if failed?
                       colors/red
                       colors/blue)})

(defn progress-bar-todo
  [todo failed?]
  {:flex             todo
   :background-color (if failed?
                       colors/red
                       colors/blue)
   :opacity          0.30})

(def details-confirmations-count
  {:margin-vertical 2})

(def details-failed
  {:color           colors/red
   :margin-vertical 2})

(def details-confirmations-helper-text
  {:color           colors/gray
   :margin-vertical 2})

(def details-separator
  {:background-color colors/black-transparent
   :height           1
   :margin-vertical  10})
