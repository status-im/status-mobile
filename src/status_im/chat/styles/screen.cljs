(ns status-im.chat.styles.screen
  (:require [status-im.components.styles :refer [chat-background
                                                 selected-message-color
                                                 separator-color
                                                 text1-color
                                                 text2-color]]
            [status-im.components.toolbar.styles :refer [toolbar-background1]]))

(def chat-view
  {:flex             1
   :background-color chat-background})

(def toolbar-container
  {})

(defn messages-container [bottom]
  {:flex           1
   :padding-bottom bottom
   :margin-bottom  0})

(def toolbar-view
  {:flexDirection   :row
   :height          56
   :backgroundColor toolbar-background1
   :elevation       2})

(def action
  {:width          56
   :height         56
   :top            0
   :alignItems     :center
   :justifyContent :center})

(def icon-view
  {:width  56
   :height 56})

(def back-icon
  {:marginTop  21
   :marginLeft 23
   :width      8
   :height     14})

(defn chat-name-view [show-actions]
  {:flex            1
   :margin-bottom   2
   :margin-left     (if show-actions 16 0)
   :align-items     :flex-start
   :justify-content :center})

(def chat-name-text
  {:color      text1-color
   :margin-top 2
   :fontSize   16})

(def group-icon
  {:margin-top    4
   :margin-bottom 2.7
   :width         14
   :height        9})

(def up-icon
  {:width  14
   :height 8})

(def members
  {:marginTop  -0.5
   :marginLeft 4
   :fontSize   12
   :color      text2-color})

(def last-activity
  {:height 18})

(defn actions-wrapper [status-bar-height]
  {:backgroundColor toolbar-background1
   :elevation       2
   :position        :absolute
   :top             (+ 55 status-bar-height)
   :left            0
   :right           0})

(def actions-separator
  {:marginLeft      16
   :height          1.5
   :backgroundColor separator-color})

(def actions-view
  {:marginVertical 10})

(def action-icon-row
  {:flexDirection :row
   :height        56})

(def action-icon-view
  (merge icon-view
         {:alignItems     :center
          :justifyContent :center}))

(def action-view
  {:flex           1
   :alignItems     :flex-start
   :justifyContent :center})

(def action-title
  {:margin-top -2.5
   :color      text1-color
   :font-size  14})

(def action-subtitle
  {:margin-top 1
   :color      text2-color
   :font-size  12})

(def actions-overlay
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(def typing-all
  {:marginBottom 20})

(def typing-view
  {:width        260
   :marginTop    10
   :paddingLeft  8
   :paddingRight 8
   :alignItems   :flex-start
   :alignSelf    :flex-start})

(def typing-background
  {:borderRadius    14
   :padding         12
   :height          38
   :backgroundColor selected-message-color})

(def typing-text
  {:marginTop -2
   :fontSize  12
   :color     text2-color})

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
  {:backgroundColor toolbar-background1
   :elevation       2
   :position        :absolute
   :bottom          16
   :left            16
   :right           16
   :height          height})

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

(def bottom-info-row-photo
  {:width        42
   :height       42
   :borderRadius 21})

(def bottom-info-row-text-container
  {:margin-left  16
   :margin-right 16})

(def bottom-info-row-text1
  {:color "black"})

(def bottom-info-row-text2
  {:color "#888888"})

(def chat-modal
  {:position :absolute
   :left     0
   :top      0
   :right    0
   :bottom   0})

(def add-contact
  {:height           35
   :background-color :white
   :justify-content  :center})

(def add-contact-text
  {:text-align          :center
   :text-align-vertical :center
   :color               :#7099e6})
