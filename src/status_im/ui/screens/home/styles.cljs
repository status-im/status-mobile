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

(def last-message-text
  {:flex        1
   :align-self  :stretch
   :line-height 22
   :color       colors/gray})

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
   :align-items        :center
   :justify-content    :center
   :padding-horizontal 34
   :align-self         :stretch
   :background-color   :white
   :transform          [{:translateY (- search-input-height)}]})

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
