(ns status-im.chat.commands.impl.transactions.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]))

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

(defn command-send-status-icon
  [outgoing]
  {:background-color (if outgoing
                       colors/black-transparent
                       colors/blue-light)
   :width            24
   :height           24
   :border-radius    16
   :padding-top      4
   :padding-left     4})

(defn command-send-status-text
  [outgoing]
  {:typography  :caption
   :color       (if outgoing
                  colors/white-transparent
                  colors/blue)
   :margin-top  4
   :margin-left 6})

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

(defn command-send-amount-text
  [outgoing]
  {:font-size   22
   :line-height 28
   :font-weight "600"
   :color       (if outgoing colors/white colors/blue)})

(def command-send-currency
  {:flex-direction :column
   :align-items    :flex-end})

(defn command-amount-currency-separator [outgoing]
  {:opacity 0
   :color   (if outgoing colors/blue colors/blue-light)})

(defn command-send-currency-text [outgoing]
  {:font-size   22
   :margin-left 4
   :color       (if outgoing colors/white-transparent colors/gray)})

(defn command-request-currency-text [outgoing]
  {:font-size 22
   :color     (if outgoing colors/wild-blue-yonder colors/gray)})

(defn command-request-timestamp-text [outgoing]
  {:font-size 12
   :color     (if outgoing colors/wild-blue-yonder colors/gray)})

(def command-send-fiat-amount
  {:flex-direction  :column
   :justify-content :flex-end
   :margin-top      6})

(defn command-send-fiat-amount-text [outgoing]
  {:typography :caption
   :color      (if outgoing colors/white colors/black)})

(def command-send-recipient-text
  {:color       colors/blue
   :font-size   14})

(defn command-send-timestamp [outgoing]
  {:color      (if outgoing colors/white-transparent colors/gray)
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
   :background-color (if outgoing colors/blue colors/blue-light)})

(defn command-request-header-text [outgoing]
  {:font-size 12
   :color     (if outgoing colors/wild-blue-yonder colors/gray)})

(def command-request-row
  {:flex-direction :row
   :margin-top     6})

(defn command-request-amount-text [outgoing]
  {:font-size 22
   :color     (if outgoing colors/white colors/black)})

(def command-request-separator-line
  {:background-color colors/gray-light
   :height           1
   :border-radius    8
   :margin-top       10})

(def command-request-button
  {:align-items :center
   :padding-top 8})

(defn command-request-button-text [answered?]
  {:color (if answered? colors/gray colors/blue)})

(def command-request-fiat-amount-row
  {:margin-top 6})

(defn command-request-fiat-amount-text [outgoing]
  {:font-size 12
   :color     (if outgoing colors/white colors/black)})

(def command-request-recipient-text
  {:color       colors/blue
   :font-size   14})

(def command-request-network-text
  {:color colors/red})

(def command-request-timestamp-row
  {:margin-top 6})
