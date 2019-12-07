(ns status-im.ui.screens.home.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.styles :as styles]))

(defn toolbar []
  {:background-color colors/white})

(def sync-wrapper
  {:flex-direction :row})

(def sync-info
  {:margin-horizontal 15})

(def last-message-container
  {:flex-shrink 1})

(styles/def last-message-text
  {:flex        1
   :align-self  :stretch
   :line-height 21
   :color       colors/gray
   :desktop     {:max-height 20}})

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

(styles/def search-input
  {:flex 1
   :android {:margin  0
             :padding 0}})

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

(def datetime-text
  {:color          colors/text-gray
   :font-size      10
   :text-align     :right
   :letter-spacing 0.4
   :align-items    :center
   :line-height    12})

(styles/def new-messages-text
  {:left       0
   :font-size  12
   :color      colors/blue
   :text-align :center
   :android    {:top 2}
   :ios        {:top 3}
   :desktop    {:top 3}})

(def group-icon
  {:margin-top   8
   :margin-right 6
   :width        14
   :height       9
   :tint-color   :white})

(def no-chats
  {:flex               1
   :padding-top        16
   :padding-horizontal 16
   :background-color   :white})

(def chat-tooltip
  {:align-items   :center
   :border-color  colors/gray-lighter
   :border-width  1
   :border-radius 16
   :margin        16})

(def no-chats-text
  {:margin-top        50
   :margin-horizontal 16
   :line-height       22
   :text-align        :center})

(def welcome-view
  {:flex            1
   :justify-content :flex-end})

(def welcome-image-container
  {:align-items :center})

(def welcome-text
  {:typography :header
   :text-align :center})

(def welcome-blank-text
  {:font-size   15
   :width       270
   :line-height 22
   :text-align  :center
   :color       colors/gray})

(def welcome-text-description
  {:margin-top        16
   :margin-bottom     32
   :text-align        :center
   :margin-horizontal 40
   :color             colors/gray})

(defn action-button-container [home-width]
  {:position    :absolute
   :z-index     2
   :align-items :center
   :bottom      16
   :left        (- (/ home-width 2) 20)
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

(def empty-chats-header-container
  {:align-items     :center
   :justify-content :center})

(def hr-wrapper
  {:position         :absolute
   :width            "100%"
   :height           1
   :top              9
   :border-top-width 1
   :border-color     colors/gray-lighter})

(def or-text
  {:width            40
   :background-color colors/white
   :font-size        12
   :text-align       :center
   :color            colors/gray})

(def tags-wrapper
  {:margin-top      10
   :margin-bottom   18})

(def tag-text
  {:font-size     13
   :font-weight   "500"
   :line-height   20
   :margin-left   10
   :margin-right  10
   :margin-top    6
   :margin-bottom 6
   :color         colors/blue})

(def close-icon-container
  {:width            24
   :height           24
   :border-radius    12
   :background-color colors/gray
   :align-items      :center
   :justify-content  :center})
