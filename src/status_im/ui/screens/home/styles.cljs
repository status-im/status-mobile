(ns status-im.ui.screens.home.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as component.styles]))

(defn toolbar []
  {:background-color component.styles/color-white})

(def chat-separator-item
  {:border-bottom-width 1
   :border-bottom-color component.styles/color-gray5})

(defstyle chat-container
  {:flex-direction   :row
   :background-color component.styles/color-white
   :android          {:height 76}
   :ios              {:height 74}
   :overflow         "hidden"})

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
  {:color     component.styles/text1-color
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
  {:color   component.styles/text4-color
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
  {:color   component.styles/text4-color
   :android {:font-size 14}
   :ios     {:font-size 15}})

(def new-messages-container
  {:min-width          22
   :height             22
   :padding-horizontal 8
   :margin-left        15
   :background-color   component.styles/new-messages-count-color
   :border-radius      50})

(defstyle new-messages-text
  {:left       0
   :fontSize   12
   :color      component.styles/color-blue
   :text-align :center
   :android    {:top 2}
   :ios        {:top 3}})

(def chats-container
  {:flex 1})

(defstyle list-container
  {:android {:background-color component.styles/color-light-gray}

   :ios     {:background-color component.styles/color-white}})

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
   :color    component.styles/color-white})

(def group-icon
  {:margin-top   8
   :margin-right 6
   :width        14
   :height       9
   :tint-color   :white})
