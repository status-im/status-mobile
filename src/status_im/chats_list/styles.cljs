(ns status-im.chats-list.styles
  (:require [status-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               color-blue
                                               online-color
                                               text1-color
                                               text2-color
                                               new-messages-count-color]]
            [status-im.components.tabs.styles :refer [tab-height]]))

(def chat-container
  {:flexDirection     :row
   :paddingVertical   15
   :paddingHorizontal 16
   :height            90})

(def chat-icon-container
  {:marginTop  -2
   :marginLeft -4
   :width      48
   :height     48})

(def item-container
  {:flexDirection :column
   :marginLeft    12
   :flex          1})

(def name-view
  {:flexDirection :row})

(def name-text
  {:marginTop  -2.5
   :color      text1-color
   :fontSize   14
   :fontFamily title-font})

(def group-icon
  {:marginTop  4
   :marginLeft 8
   :width      14
   :height     9})

(def memebers-text
  {:marginTop  -0.5
   :marginLeft 4
   :fontFamily font
   :fontSize   12
   :color      text2-color})

(def last-message-text
  {:marginTop   7
   :marginRight 40
   :color       text1-color
   :fontFamily  font
   :fontSize    14
   :lineHeight  20})

(def status-container
  {:flexDirection :row
   :position      :absolute
   :top           0
   :right         0})

(def status-image
  {:marginTop 6
   :width     9
   :height    7})

(def datetime-text
  {:fontFamily font
   :fontSize   12
   :color      text2-color
   :marginLeft 5})

(def new-messages-container
  {:position        :absolute
   :top             36
   :right           0
   :width           24
   :height          24
   :backgroundColor new-messages-count-color
   :borderRadius    50})

(def new-messages-text
  {:top        4
   :left       0
   :fontFamily title-font
   :fontSize   10
   :color      color-blue
   :textAlign  :center})

(def hamburger-icon
  {:width  16
   :height 12})

(def search-icon
  {:width  17
   :height 17})

(def chats-container
  {:flex            1
   :backgroundColor :white})

(def list-container
  {:backgroundColor :white
   :marginBottom    tab-height})

(def create-icon
  {:fontSize 20
   :height   22
   :color    :white})

(def person-stalker-icon
  {:fontSize 20
   :height   22
   :color    :white})
