(ns status-im.transactions.styles.screens
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.components.styles :as st]
            [status-im.utils.platform :as platform]))

;; common

(def transactions-toolbar-background (if platform/ios?
                                       st/color-dark-blue-2
                                       st/color-dark-blue-1))

(defstyle toolbar-title-container
  {:flex           1
   :flex-direction :row
   :align-self     :stretch
   :padding-left   30
   :ios            {:align-items     :center
                    :justify-content :center
                    :padding-bottom  16}})

(def toolbar-title-text
  {:color     st/color-white
   :font-size 17})

(defstyle toolbar-title-count
  {:color       st/color-white
   :font-size   17
   :margin-left 8
   :android {:opacity 0.2}
   :ios     {:opacity 0.6}})

(defstyle toolbar-border
  {:ios {:background-color st/color-white
         :opacity          0.1}})

;; pending-transactions

(def transactions-screen
  {:flex             1
   :background-color st/color-dark-blue-2})

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

(def details-separator
  {:margin-bottom 10
   :margin-left   16
   :opacity       0.1})

(def details-item
  {:margin-top     10
   :padding-left   16
   :padding-right  16
   :flex-direction :row})

(defstyle details-item-title
  {:width     80
   :font-size 15
   :color     st/color-white
   :android   {:opacity      0.2
               :margin-right 24}
   :ios       {:opacity      0.5
               :margin-right 8
               :text-align   :right}})

(defn details-item-content [name?]
  {:font-size   15
   :flex-shrink 1
   :color       (if name? st/color-light-blue st/color-white)})

(defstyle details-data
  {:margin-top       16
   :padding          16
   :background-color st/color-dark-blue-3
   :ios              {:margin-horizontal 16}})

(defstyle details-data-title
  {:font-size 15
   :color     st/color-white
   :android   {:opacity 0.2}
   :ios       {:opacity 0.5}})

(def details-data-content
  {:font-size  15
   :color      st/color-white
   :margin-top 8})

;; confirmation-success

(def success-screen
  {:flex             1
   :background-color st/color-dark-blue-2})

(def success-screen-content-container
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def success-icon-container
  {:background-color st/color-light-blue
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
   :color      st/color-light-blue3
   :margin-top 26})
