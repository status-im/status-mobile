(ns status-im.chat.styles.screen
  (:require [status-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               chat-background
                                               online-color
                                               selected-message-color
                                               separator-color
                                               text1-color
                                               text2-color
                                               toolbar-background1]]))

(def chat-view
  {:flex            1
   :backgroundColor chat-background})

(defn messages-container [bottom]
  {:flex   1
   :bottom bottom})

(def toolbar-view
  {:flexDirection   :row
   :height          56
   :backgroundColor toolbar-background1
   :elevation       2})

(def action
  {:width          56
   :height         56
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
  {:flex           1
   :marginLeft     (if show-actions 16 0)
   :alignItems     :flex-start
   :justifyContent :center})

(def chat-name-text
  {:marginTop  -2.5
   :color      text1-color
   :fontSize   16
   :fontFamily font})

(def group-icon
  {:marginTop 4
   :width     14
   :height    9})

(def up-icon
  {:width  14
   :height 8})

(def members
  {:marginTop  -0.5
   :marginLeft 4
   :fontFamily font
   :fontSize   12
   :color      text2-color})

(def last-activity
  {:marginTop  1
   :color      text2-color
   :fontSize   12
   :fontFamily font})

(def actions-wrapper
  {:backgroundColor toolbar-background1
   :elevation       2
   :position        :absolute
   :top             56
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
  {:marginTop  -2.5
   :color      text1-color
   :fontSize   14
   :fontFamily font})

(def action-subtitle
  {:marginTop  1
   :color      text2-color
   :fontSize   12
   :fontFamily font})

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
  {:marginTop  -2
   :fontSize   12
   :fontFamily font
   :color      text2-color})

(def actions-overlay
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(def overlay-highlight
  {:flex 1})
