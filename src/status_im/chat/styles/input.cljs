(ns status-im.chat.styles.input
  (:require [status-im.components.styles :refer [color-white
                                                 color-blue
                                                 text1-color
                                                 text2-color
                                                 chat-background
                                                 color-black]]))

(def command-input-and-suggestions-container
  {:flexDirection :column})

(def command-input-container
  {:flexDirection   :row
   :height          56
   :backgroundColor color-white})

(def command-container
  {:left            0
   :backgroundColor :white
   :position        :absolute})

(defn command-text-container
  [{:keys [color]}]
  {:flexDirection   :column
   :marginTop       16
   :marginBottom    16
   :marginLeft      16
   :marginRight     8
   :backgroundColor color
   :height          24
   :borderRadius    50})

(def command-text
  {:marginTop        3
   :marginHorizontal 12
   :fontSize         12
   :color            color-white})

(def command-input
  {:flex       1
   :marginLeft 8
   :marginTop  -2
   :padding    0
   :fontSize   14
   :color      text1-color})

(def send-container
  {:marginTop       10
   :marginRight     10
   :width           36
   :height          36
   :borderRadius    50
   :backgroundColor color-blue})

(def send-icon
  {:marginTop  10.5
   :marginLeft 12
   :width      15
   :height     15})

(def cancel-container
  {:marginTop   10
   :marginRight 10
   :width       36
   :height      36})

(def cancel-icon
  {:marginTop  10.5
   :marginLeft 12
   :width      12
   :height     12})

(defn staged-commands [message-input-height input-margin]
  {:position         :absolute
   :background-color color-white
   :bottom           (+ message-input-height input-margin)
   :left             0
   :right            0
   :max-height       150
   :elevation        5})

(def staged-command-container
  {:flex            1
   :alignItems      :flex-start
   :flexDirection   :column
   :backgroundColor color-white})

(def staged-command-background
  {:flexDirection   :column
   :margin-top      16
   :margin-left     16
   :margin-right    16
   :padding-bottom  12
   :padding-left    12
   :backgroundColor chat-background
   :borderRadius    14})

(def staged-command-info-container
  {:flexDirection :row
   :margin-top    12})

(def staged-command-cancel
  {:padding-left  12
   :padding-top   16
   :padding-right 12})

(def staged-command-cancel-icon
  {:width  16
   :height 16})

(def staged-command-content
  {:marginTop        5
   :marginHorizontal 0
   :fontSize         14
   :color            color-black})

(def staged-commands-bottom
  {:height 16
   :background-color "white"})
