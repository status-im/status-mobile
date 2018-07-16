(ns status-im.chat.commands.impl.transactions.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as styles]))

(def asset-container
  {:flex-direction   :row
   :align-items      :center
   :justify-content  :space-between
   :padding-vertical 11})

(def asset-main
  {:flex            1
   :flex-direction  :row
   :align-items     :center})

(def asset-icon
  {:width        30
   :height       30
   :margin-left  14
   :margin-right 12})

(def asset-symbol
  {:color colors/black})

(def asset-name
  {:color        colors/gray
   :padding-left 4})

(def asset-balance
  {:color         colors/gray
   :padding-right 14})

(def asset-separator
  {:height           1
   :background-color colors/gray-light
   :margin-left      56})

(def command-send-status-container
  {:margin-top     6
   :flex-direction :row})

(defn command-send-status-icon [outgoing]
  {:background-color (if outgoing
                       colors/blue-darker
                       colors/blue-transparent)
   :width            24
   :height           24
   :border-radius    16
   :padding-top      4
   :padding-left     4})

(defstyle command-send-status-text
  {:color       colors/blue
   :android     {:margin-top 3}
   :ios         {:margin-top 4}
   :margin-left 6
   :font-size   12})

(def command-send-message-view
  {:flex-direction :column
   :align-items    :flex-start})

(def command-send-amount-row
  {:flex-direction  :row
   :justify-content :space-between})

(def command-send-amount
  {:flex-direction :column
   :align-items    :flex-end
   :max-width      250})

(defstyle command-send-amount-text
  {:font-size   22
   :color       colors/blue
   :ios         {:letter-spacing -0.5}})

(def command-send-currency
  {:flex-direction :column
   :align-items    :flex-end})

(defn command-amount-currency-separator [outgoing]
  {:opacity 0
   :color (if outgoing colors/hawkes-blue colors/white)})

(defn command-send-currency-text [outgoing]
  {:font-size      22
   :margin-left    4
   :letter-spacing 1
   :color          (if outgoing colors/wild-blue-yonder colors/blue-transparent-40)})

(defn command-request-currency-text [outgoing]
  {:font-size      22
   :letter-spacing 1
   :color          (if outgoing colors/wild-blue-yonder colors/gray)})

(defn command-request-timestamp-text [outgoing]
  {:font-size 12
   :color     (if outgoing colors/wild-blue-yonder colors/gray)})

(def command-send-fiat-amount
  {:flex-direction  :column
   :justify-content :flex-end
   :margin-top      6})

(def command-send-fiat-amount-text
  {:font-size 12
   :color     colors/black})

(def command-send-recipient-text
  {:color       colors/blue
   :font-size   14
   :line-height 18})

(defn command-send-timestamp [outgoing]
  {:color      (if outgoing colors/wild-blue-yonder colors/gray)
   :margin-top 6
   :font-size  12})

(def command-request-image-touchable
  {:position        :absolute
   :top             0
   :right           -8
   :align-items     :center
   :justify-content :center
   :width           48
   :height          48})

(defn command-request-image-view [command scale]
  {:width            32
   :height           32
   :border-radius    16
   :background-color (:color command)
   :transform        [{:scale scale}]})

(def command-request-image
  {:position :absolute
   :top      9
   :left     10
   :width    12
   :height   13})

(defn command-request-message-view [outgoing]
  {:border-radius    14
   :padding-vertical 4
   :background-color (if outgoing colors/hawkes-blue styles/color-white)})

(defn command-request-header-text [outgoing]
  {:font-size 12
   :color     (if outgoing colors/wild-blue-yonder colors/gray)})

(def command-request-row
  {:flex-direction :row
   :margin-top     6})

(defstyle command-request-amount-text
  {:font-size   22
   :ios         {:letter-spacing -0.5}
   :color       colors/black})

(def command-request-separator-line
  {:background-color colors/gray-light
   :height           1
   :border-radius    8
   :margin-top       10})

(def command-request-button
  {:align-items :center
   :padding-top 8})

(defn command-request-button-text [answered?]
  {:font-size 15
   :color     (if answered? colors/gray colors/blue)})

(def command-request-fiat-amount-row
  {:margin-top 6})

(def command-request-fiat-amount-text
  {:font-size 12
   :color     colors/black})

(def command-request-recipient-text
  {:color       colors/blue
   :font-size   14
   :line-height 18})

(def command-request-network-text
  {:color colors/red})

(def command-request-timestamp-row
  {:margin-top 6})

