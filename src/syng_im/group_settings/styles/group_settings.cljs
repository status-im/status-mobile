(ns syng-im.group-settings.styles.group-settings
  (:require [syng-im.components.styles :refer [font
                                               font-medium
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

(def modal-container
  {:flex           1
   :justifyContent :center
   :padding        20})

(def modal-inner-container
  {:borderRadius    10
   :alignItems      :center
   :padding         5
   :backgroundColor color-white})

(def modal-member-name
  {:color        text2-color
   :fontFamily   font
   :fontSize     14
   :lineHeight   20})

(def modal-remove-text
  {:margin       10
   :color        text1-color
   :fontFamily   font
   :fontSize     14
   :lineHeight   20})

(def chat-members-container
  {:marginBottom 10})

(def chat-icon
  {:margin          10
   :width           36
   :height          36
   :borderRadius    50
   :backgroundColor color-purple})

(def chat-icon-text
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
   :fontFamily   font-medium
   :fontSize     14
   :lineHeight   20})

(def chat-name-value-container
  {:flexDirection     :row
   :marginLeft        16
   :height            56
   :alignItems        :center
   :justifyContent    :center
   :borderBottomWidth 1
   :borderBottomColor separator-color})

(def chat-name-value
  {:flex       1
   :fontSize   16
   :fontFamily font
   :color      text1-color})

(def chat-name-btn-edit-container
  {:padding        16
   :justifyContent :center})

(def chat-name-btn-edit-text
  {:marginTop    -1
   :color        text2-color
   :fontFamily   font
   :fontSize     16
   :lineHeight   20})

(def members-text
  {:marginTop    24
   :marginLeft   16
   :marginBottom 16
   :color        text2-color
   :fontFamily   font-medium
   :fontSize     14
   :lineHeight   20})

(def add-members-icon
  {:marginVertical   19
   :marginLeft       19
   :marginHorizontal 3
   :width            17
   :height           17})

(def add-members-container
  {:flexDirection :row})

(def add-members-text
  {:marginTop  18
   :marginLeft 32
   :color      text2-color
   :fontFamily font
   :fontSize   16
   :lineHeight 20})

(def settings-text
  {:marginTop    24
   :marginLeft   16
   :marginBottom 16
   :color        text2-color
   :fontFamily   font-medium
   :fontSize     14
   :lineHeight   20})
