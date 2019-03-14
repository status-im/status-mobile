(ns status-im.ui.screens.chat.styles.main
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as component.styles]
            [status-im.ui.components.colors :as colors]))

(def scroll-root
  {:flex 1})

(def chat-view
  {:flex             1
   :background-color colors/white})

(def toolbar-container
  {:flex           1
   :flex-direction :row})

(def messages-container
  {:flex           1
   :padding-bottom 0
   :margin-bottom  0})

(def action
  {:width           56
   :height          56
   :top             0
   :align-items     :center
   :justify-content :center})

(def icon-view
  {:width  56
   :height 56})

(def back-icon
  {:margin-top  21
   :margin-left 23
   :width       8
   :height      14})

(def chat-toolbar-contents
  {:flex-direction :row
   :flex           1})

(def chat-name-view
  {:flex            1
   :justify-content :center})

(def chat-name-text
  {:typography :main-medium
   :margin-top -3})

(def group-icon
  {:margin-top    4
   :margin-bottom 2.7
   :width         14
   :height        9})

(def toolbar-subtitle
  {:typography  :caption
   :color       colors/text-gray})

(def last-activity-text
  {:font-size  14
   :margin-top 4
   :color      colors/text-gray})

(defn actions-wrapper [status-bar-height]
  {:background-color colors/white
   :elevation        2
   :position         :absolute
   :top              (+ 55 status-bar-height)
   :left             0
   :right            0})

(def actions-separator
  {:margin-left      16
   :height           1.5
   :background-color colors/black-transparent})

(def actions-view
  {:margin-vertical 10})

(def action-icon-row
  {:flex-direction :row
   :height         56})

(def action-icon-view
  (merge icon-view
         {:align-items     :center
          :justify-content :center}))

(def action-view
  {:flex            1
   :align-items     :flex-start
   :justify-content :center})

(def action-title
  {:margin-top -2.5
   :color      colors/text
   :font-size  14})

(def typing-all
  {:marginBottom 20})

(def typing-view
  {:width         260
   :margin-top    10
   :padding-left  8
   :padding-right 8
   :align-items   :flex-start
   :align-self    :flex-start})

(def typing-text
  {:margin-top -2
   :font-size  12
   :color      colors/text-gray})

(def overlay-highlight
  {:flex 1})

;; this map looks a bit strange
;; but this way of setting elevation seems to be the only way to set z-index (in RN 0.30)
(def bottom-info-overlay
  {:position         :absolute
   :top              -16
   :bottom           -16
   :left             -16
   :right            -16
   :background-color "#00000055"
   :elevation        8})

(defn bottom-info-container [height]
  {:background-color colors/white
   :elevation        2
   :position         :absolute
   :bottom           16
   :left             16
   :right            16
   :height           height})

(def bottom-info-list-container
  {:padding-left   16
   :padding-right  16
   :padding-top    8
   :padding-bottom 8})

(def item-height 60)

(def bottom-info-row
  {:flex-direction "row"
   :padding-top    4
   :padding-bottom 4})

(def bottom-info-row-photo-size 42)

(def bottom-info-row-text-container
  {:margin-left  16
   :margin-right 16})

(def bottom-info-row-text1
  {:color "black"})

(def bottom-info-row-text2
  {:color "#888888"})

(def add-contact
  {:flex-direction      :row
   :align-items         :center
   :height              36
   :background-color    :white
   :border-bottom-width 1
   :border-color        colors/gray-lighter
   :justify-content     :space-between})

(def add-contact-left
  {:width       24
   :margin-left 12})

(def add-contact-center
  {:flex-direction :row
   :align-items    :center})

(def add-contact-text
  {:text-align          :center
   :text-align-vertical :center
   :padding-left        4
   :color               colors/blue})

(def add-contact-close-icon
  {:margin-right 12})

(def message-view-preview
  {:flex            1
   :align-items     :center
   :justify-content :center})

(defn message-view-animated [opacity]
  {:opacity opacity
   :flex    1})

(def empty-chat-container-one-to-one
  {:margin-top 10})

(def empty-chat-container
  {:flex             1
   :flex-direction   :column
   :justify-content  :center
   :align-items      :center
   :padding-vertical 50
   :margin-right     6})

(def empty-chat-text
  {:color      colors/gray
   :width      280
   :text-align :center})

(def empty-chat-text-name
  {:margin-bottom 5})

(def join-button
  {:margin-top    24
   :margin-bottom 15})

(def group-chat-icon
  {:color       colors/white
   :font-size   40
   :font-weight "700"})

(def group-chat-join-footer
  {:position        :absolute
   :justify-content :center
   :margin-bottom   30
   :bottom          0})

(def group-chat-join-name
  {:typography :header})

(def group-chat-join-container
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def decline-chat
  {:color colors/blue})
