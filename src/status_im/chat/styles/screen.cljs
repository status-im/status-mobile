(ns status-im.chat.styles.screen
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as component.styles]
            [status-im.ui.components.colors :as colors]))

(def scroll-root
  {:flex 1})

(def chat-view
  {:flex             1
   :background-color component.styles/chat-background})

(def toolbar-container
  {:flex           1
   :flex-direction :row
   :align-items    :stretch
   :margin-left    3})

(def messages-container
  {:flex           1
   :padding-bottom 0
   :margin-bottom  0})

(def toolbar-view
  {:flex-direction   :row
   :height           56
   :background-color component.styles/color-white
   :elevation        2})

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
   :justify-content :center
   :margin-bottom   2})

(def chat-name-text
  {:color       component.styles/color-black
   :line-height 18
   :font-size   15})

(def group-icon
  {:margin-top    4
   :margin-bottom 2.7
   :width         14
   :height        9})

(def up-icon
  {:width  14
   :height 8})

(defstyle toolbar-subtitle
  {:color       component.styles/text4-color
   :line-height 15
   :font-size   13
   :ios         {:margin-top 4}})

(defstyle last-activity-text
  {:color       component.styles/text4-color
   :line-height 15
   :ios         {:font-size  14
                 :margin-top 4}
   :android     {:font-size 13}})

(defn actions-wrapper [status-bar-height]
  {:background-color component.styles/color-white
   :elevation        2
   :position         :absolute
   :top              (+ 55 status-bar-height)
   :left             0
   :right            0})

(def actions-separator
  {:margin-left      16
   :height           1.5
   :background-color component.styles/separator-color})

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
   :color      component.styles/text1-color
   :font-size  14})

(def action-subtitle
  {:margin-top 1
   :color      component.styles/text2-color
   :font-size  12})

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
   :color      component.styles/text2-color})

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
  {:background-color component.styles/color-white
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
  {:flex-direction   :row
   :align-items      :center
   :height           36
   :background-color colors/white
   :justify-content  :space-between})

(def add-contact-left
  {:width       24
   :margin-left 12})

(def add-contact-center
  {:flex-direction :row
   :align-items    :center})

(defstyle add-contact-text
  {:text-align          :center
   :text-align-vertical :center
   :padding-left        4
   :font-size           15
   :ios                 {:letter-spacing 0.2}
   :color               colors/blue})

(def add-contact-close-icon
  {:margin-right 12})

(defn scroll-to-bottom-button [have-unreads? many-messages?]
  {:width            (if have-unreads?
                       (if many-messages? 76 58)
                       36)
   :height           36
   :background-color colors/blue
   :justify-content  :center
   :border-radius    20
   :position         :absolute
   :bottom           80
   :right            30})

(def scroll-to-bottom-button-inner
  {:align-items     :center
   :justify-content :center
   :flex-direction  :row})

(def scroll-to-bottom-button-text
  {:font-size    15
   :padding-left 4
   :color        colors/white})

(defn scroll-to-bottom-button-icon [have-unreads?]
  (when have-unreads?
    {:padding-left 2}))

(defstyle actions-list-view
  {:ios {:border-bottom-color component.styles/color-gray3
         :border-bottom-width 0.5}})

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
  {:color          colors/gray
   :width          280
   :font-size      15
   :line-height    22
   :letter-spacing -0.2
   :text-align     :center})

(def empty-chat-text-name
  {:color colors/black})
