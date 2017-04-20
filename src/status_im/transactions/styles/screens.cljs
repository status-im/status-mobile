(ns status-im.transactions.styles.screens
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.components.styles :as common]
            [status-im.utils.platform :as platform]))

;; common

(def transactions-toolbar-background (if platform/ios?
                                       common/color-dark-blue-2
                                       common/color-dark-blue-1))

(defstyle toolbar-title-container
  {:flex           1
   :flex-direction :row
   :padding-left   18
   :ios            {:align-items     :center
                    :justify-content :center}})

(def toolbar-title-text
  {:color     common/color-white
   :font-size 17})

(defstyle toolbar-title-count
  {:color       common/color-white
   :font-size   17
   :margin-left 8
   :android {:opacity 0.2}
   :ios     {:opacity 0.6}})

(defstyle toolbar-border
  {:ios {:background-color common/color-white
         :opacity          0.1}})

;; unsigned-transactions

(def transactions-screen
  {:flex             1
   :background-color common/color-dark-blue-2})

(def transactions-screen-content-container
  {:flex            1
   :justify-content :space-between})

(defstyle transactions-list
  {:flex 1
   :android {:padding-vertical 8}})

(def transactions-list-separator
  {:margin-left   16
   :opacity       0.1})

;; transaction-details

(defstyle details-screen-content-container
  {:flex    1
   :android {:padding-top 8}})

(def details-separator-wrapper
  {:background-color common/color-dark-blue-2})

(def details-separator
  {:margin-left   16
   :background-color common/color-white
   :opacity       0.1})

(defstyle details-container
  {:ios     {:margin-top 10}
   :android {:margin-top 0}})

(def details-item
  {:margin-top     10
   :padding-left   16
   :padding-right  16
   :flex-direction :row})

(defstyle details-item-title
  {:width     80
   :font-size 15
   :color     common/color-white
   :android   {:opacity      0.2
               :margin-right 24}
   :ios       {:opacity      0.5
               :margin-right 8
               :text-align   :right}})

(defn details-item-content [name?]
  {:font-size   15
   :flex-shrink 1
   :color       (if name? common/color-light-blue common/color-white)})

(defstyle details-data
  {:padding          16
   :margin-top       16
   :background-color common/color-dark-blue-3
   :ios              {:margin-horizontal 16}})

(defstyle details-data-title
  {:font-size 15
   :color     common/color-white
   :android   {:opacity 0.2}
   :ios       {:opacity 0.5}})

(def details-data-content
  {:font-size  15
   :color      common/color-white
   :margin-top 8})

;; confirmation-success

(def success-screen
  {:flex             1
   :background-color common/color-dark-blue-2})

(def success-screen-content-container
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def success-icon-container
  {:background-color common/color-light-blue
   :border-radius    100
   :height           133
   :width            133
   :justify-content  :center
   :align-items      :center})

(def success-icon
  {:height 40
   :width  54})

(def success-text
  {:font-size  17
   :color      common/color-light-blue3
   :margin-top 26})
