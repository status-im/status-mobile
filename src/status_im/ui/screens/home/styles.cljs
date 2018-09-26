(ns status-im.ui.screens.home.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as component.styles]
            [status-im.ui.components.colors :as colors]))

(def delete-button-width 100)

(defn toolbar []
  {:background-color colors/white})

(def chat-separator-item
  {:border-bottom-width 1
   :border-bottom-color component.styles/color-gray5})

(defstyle chat-container
  {:flex-direction   :row
   :background-color colors/white
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

(def browser-icon-container
  {:width            40
   :height           40
   :border-radius    20
   :background-color colors/gray-lighter
   :align-items      :center
   :justify-content  :center})

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
  {:color     component.styles/text1-color
   :android {:font-size 16
             :height    26}
   :ios     {:font-size 17
             :height    26}})

(defstyle private-group-icon-container
  {:width        20
   :height       12
   :margin-right 6})

(defstyle public-group-icon-container
  {:width        20
   :height       12
   :margin-right 6})

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
   :desktop {:font-size 14}
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
   :font-size  12
   :color      colors/blue
   :text-align :center
   :android    {:top 2}
   :ios        {:top 3}})

(def container
  {:flex 1})

(def native-button-offset 16)

(def create-icon
  {:font-size 20
   :height    22
   :color     colors/white})

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
  {:line-height    21
   :text-align     :center
   :color          colors/gray})

(def welcome-view
  {:flex 1})

(defstyle welcome-image-container
  {:align-items :center
   :android     {:margin-top 38}
   :ios         {:margin-top 42}})

(def welcome-image
  {:width  320
   :height 278})

(defstyle welcome-text
  {:line-height    28
   :font-size      22
   :font-weight    :bold
   :letter-spacing -0.3
   :android        {:margin-top 22}
   :ios            {:margin-top 96}
   :text-align     :center
   :color          colors/black})

(defstyle welcome-text-description
  {:line-height    21
   :margin-top     8
   :android        {:margin-bottom 82}
   :ios            {:margin-bottom 32}
   :text-align     :center
   :color          colors/gray})

(def toolbar-logo
  {:size      42
   :icon-size 17
   :shadow?   false})

(def delete-icon-highlight
  {:position         :absolute
   :top              0
   :bottom           0
   :right            -800
   :width            800
   :background-color colors/red-light})

(def delete-icon-container
  {:flex            1
   :width           delete-button-width
   :justify-content :center
   :align-items     :center})

(def action-button-container
  {:position :absolute
   :bottom   16
   :right    16})

(def action-button
  {:width            56
   :height           56
   :background-color colors/blue
   :border-radius    28
   :align-items      :center
   :justify-content  :center})
