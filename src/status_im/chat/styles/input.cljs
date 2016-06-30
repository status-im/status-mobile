(ns status-im.chat.styles.input
  (:require [status-im.components.styles :refer [font
                                               color-white
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
   :backgroundColor color-white
   :elevation       4})

(def command-container
  {:left 0
   :backgroundColor :white
   :position :absolute})

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
   :fontFamily       font
   :color            color-white})

(def command-input
  {:flex       1
   :marginLeft 8
   :marginTop  -2
   :padding    0
   :fontSize   14
   :fontFamily font
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

(def staged-command-container
  {:flex            1
   :alignItems      :flex-start
   :flexDirection   :column
   :backgroundColor color-white})

(def staged-command-background
  {:flexDirection   :column
   :margin          16
   :padding         12
   :backgroundColor chat-background
   :borderRadius    14})

(def staged-command-info-container
  {:flexDirection :row})

(defn staged-command-text-container
  [{:keys [color]}]
  {:backgroundColor   color
   :height            24
   :borderRadius      50
   :marginRight       32
   :paddingTop        3
   :paddingHorizontal 12})

(def staged-command-text
  {:fontSize   12
   :fontFamily font
   :color      color-white})

(def staged-command-cancel
  {:position :absolute
   :top      7
   :right    4})

(def staged-command-cancel-icon
  {:width  10
   :height 10})

(def staged-command-content
  {:marginTop        5
   :marginHorizontal 0
   :fontSize         14
   :fontFamily       font
   :color            color-black})
