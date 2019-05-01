(ns status-im.ui.screens.home.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.bottom-bar.styles :as tabs.styles]))

(defn toolbar []
  {:background-color colors/white})

(defstyle sync-wrapper
  {:flex-direction :row})

(defstyle sync-info {:margin-horizontal 15})

(defstyle chat-container
  {:flex-direction   :row
   :android          {:height 76}
   :ios              {:height 74}
   :overflow         :hidden})

(defstyle chat-icon-container
  {:padding-top    18
   :padding-bottom 18
   :padding-left   12
   :padding-right  20
   :width          72
   :android        {:height 76}
   :ios            {:height 74}})

(defstyle chat-info-container
  {:margin-bottom   13
   :justify-content :space-between
   :flex            1
   :flex-direction  :column
   :android         {:margin-top 16}
   :ios             {:margin-top 14}})

(defstyle chat-options-container
  {:padding-top 10})

(defstyle item-upper-container
  {:flex           1
   :flex-direction :row
   :padding-right  16})

(defstyle item-lower-container
  {:flex            1
   :flex-direction  :row
   :justify-content :space-between
   :padding-right   16
   :android         {:margin-top 4}
   :ios             {:margin-top 6}})

(def message-status-container
  {:flex-direction :row
   :align-items    :center})

(def name-view
  {:flex-direction :row
   :flex           1
   :margin-right   4})

(defstyle name-text
  {:color   colors/text
   :android {:font-size 16
             :height    26}
   :ios     {:font-size 17
             :height    26}})

(defstyle private-group-icon-container
  {:align-items     :center
   :justify-content :center
   :margin-right    6})

(defstyle public-group-icon-container
  {:align-items     :center
   :justify-content :center
   :margin-right    6})

(def last-message-container
  {:flex-shrink 1})

(def last-message-text
  {:color  colors/text-gray
   :height 24})

(def search-input-height 56)

(def search-container
  {:height             search-input-height
   :flex-direction     :row
   :padding-horizontal 16
   :background-color   colors/white
   :align-items        :center
   :justify-content    :center})

(def search-input-container
  {:background-color colors/gray-lighter
   :flex             1
   :flex-direction   :row
   :height           36
   :align-items      :center
   :justify-content  :center
   :border-radius    8})

(def search-input
  (merge {:flex 1}
         (when platform/android?
           {:margin      0
            :padding     0})))

(def filter-section-title
  {:margin-left   16
   :margin-top    14
   :margin-bottom 4
   :color         colors/gray})

(def status-container
  {:flex-direction :row
   :top            16
   :right          16})

(def status-image
  {:opacity      0.6
   :margin-right 4
   :width        16
   :height       16})

(defstyle datetime-text
  {:color   colors/text-gray
   :android {:font-size 12}
   :desktop {:font-size 14}
   :ios     {:font-size 12}})

(defstyle new-messages-text
  {:left       0
   :font-size  12
   :color      colors/blue
   :text-align :center
   :android    {:top 2}
   :ios        {:top 3}})

(def group-icon
  {:margin-top   8
   :margin-right 6
   :width        14
   :height       9
   :tint-color   :white})

(def no-chats
  {:flex              1
   :align-items       :center
   :justify-content   :center
   :margin-horizontal 34})

(def no-chats-text
  {:text-align     :center
   :color          colors/gray})

(def welcome-view
  {:flex 1})

(def welcome-image-container
  {:align-items :center
   :margin-top  42})

(def welcome-text
  {:typography  :header
   :margin-top  32
   :text-align  :center})

(def welcome-text-description
  {:margin-top        8
   :text-align        :center
   :margin-horizontal 32
   :color             colors/gray})

(def action-button-container
  {:position    :absolute
   :align-items :center
   :bottom      (+ tabs.styles/tabs-diff 6)
   :width       40
   :height      40})

(def action-button
  {:width            40
   :height           40
   :background-color colors/blue
   :border-radius    20
   :align-items      :center
   :justify-content  :center
   :shadow-offset    {:width 0 :height 1}
   :shadow-radius    6
   :shadow-opacity   1
   :shadow-color     "rgba(0, 12, 63, 0.2)"
   :elevation        2})
