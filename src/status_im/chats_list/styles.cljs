(ns status-im.chats-list.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.components.styles :refer [color-white
                                                 color-light-gray
                                                 color-blue
                                                 color-gray5
                                                 text1-color
                                                 text2-color
                                                 text4-color
                                                 separator-color
                                                 new-messages-count-color]]
            [status-im.components.tabs.styles :as tabs-st]
            [status-im.components.toolbar.styles :refer [toolbar-background1
                                                         toolbar-background2]]
            [status-im.utils.platform :as p]))

(defn toolbar []
  (merge {:background-color toolbar-background1}
         (get-in p/platform-specific [:component-styles :toolbar])))

(def chat-separator-item
  {:border-bottom-width 1
   :border-bottom-color color-gray5})

(defstyle chat-container
  {:flex-direction   :row
   :background-color color-white
   :android          {:height 76}
   :ios              {:height 74}})

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

(def name-text
  {:color     text1-color
   :font-size 16})

(defstyle private-group-icon-container
  {:width        16
   :height       12
   :margin-right 6
   :android      {:margin-top 4}
   :ios          {:margin-top 2}})

(def private-group-icon
  {:width  16
   :height 16})

(defstyle public-group-icon-container
  {:width        16
   :height       12
   :margin-right 6
   :android      {:margin-top 4}
   :ios          {:margin-top 2}})

(def public-group-icon
  {:width  16
   :height 16})

(def last-message-container
  {:flex-shrink 1})

(defstyle last-message-text
  {:color   text4-color
   :android {:font-size 14
             :height    24}
   :ios     {:font-size 15
             :height    24}})

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
  {:color   text4-color
   :android {:font-size 14}
   :ios     {:font-size 15}})

(def new-messages-container
  {:width            22
   :height           22
   :margin-left      15
   :background-color new-messages-count-color
   :border-radius    50})

(defstyle new-messages-text
  {:left       0
   :fontSize   12
   :color      color-blue
   :text-align :center
   :android    {:top 2}
   :ios        {:top 3}})

(def chats-container
  {:flex 1})

(defnstyle list-container [tabs-hidden?]
  {:android {:background-color color-light-gray
             :margin-bottom    20}

   :ios     {:background-color color-white
             :margin-bottom    (if tabs-hidden? 20 (+ 16 tabs-st/tabs-height))}})

(def toolbar-actions
  {:flex-direction :row
   :padding-right  14})

(def opts-btn-container
  {:align-items     :center
   :justify-content :center})

(def opts-btn
  {:padding 16})

(def create-icon
  {:fontSize 20
   :height   22
   :color    color-white})

(def group-icon
  {:margin-top   8
   :margin-right 6
   :width        14
   :height       9
   :tint-color   :white})
