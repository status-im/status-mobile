(ns syng-im.group-settings.styles.group-settings
  (:require [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               color-purple
                                               chat-background
                                               online-color
                                               selected-message-color
                                               separator-color
                                               text1-color
                                               text2-color
                                               toolbar-background1]]))

(def chat-members-container
  {:marginBottom 10})

(def save-btn
  {:margin          10
   :width           36
   :height          36
   :borderRadius    50
   :backgroundColor color-purple})

(def save-btn-text
  {:marginTop  7
   :marginLeft 13
   :color      color-white
   :fontFamily font
   :fontSize   16
   :lineHeight 20})

(def group-settings
  {:flex            1
   :flexDirection   :column
   :backgroundColor color-white})

(def chat-name-text
  {:marginTop    24
   :marginLeft   16
   :marginBottom 16
   :color        text2-color
   :fontFamily   font
   :fontSize     14
   :lineHeight   20})

(def chat-name-input
  {:marginLeft 12
   :fontSize   14
   :fontFamily font
   :color      text1-color})

(def members-text
  {:marginTop    24
   :marginLeft   16
   :marginBottom 16
   :color        text2-color
   :fontFamily   font
   :fontSize     14
   :lineHeight   20})

(def add-members-icon
  {:marginVertical   19
   :marginLeft       19
   :marginHorizontal 3
   :width            17
   :height           17})

(def add-members-container
  {:flexDirection :row
   :marginBottom  16})

(def add-members-text
  {:marginTop  18
   :marginLeft 32
   :color      text2-color
   :fontFamily font
   :fontSize   14
   :lineHeight 20})

(def settings-text
  {:marginTop    24
   :marginLeft   16
   :marginBottom 16
   :color        text2-color
   :fontFamily   font
   :fontSize     14
   :lineHeight   20})
