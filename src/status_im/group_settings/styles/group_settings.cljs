(ns status-im.group-settings.styles.group-settings
  (:require [status-im.components.styles :refer [color-white
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
  {:color      text2-color
   :fontSize   14
   :lineHeight 20})

(def modal-remove-text
  {:margin     10
   :color      text1-color
   :fontSize   14
   :lineHeight 20})

(def modal-color-picker-inner-container
  {:borderRadius    10
   :padding         5
   :backgroundColor color-white})

(def modal-color-picker-save-btn-text
  {:margin     10
   :alignSelf  :center
   :color      text1-color
   :fontSize   14
   :lineHeight 20})

(def chat-members-container
  {:marginBottom 10})

(def action
  {:width          56
   :height         56
   :alignItems     :center
   :justifyContent :center})

(def group-settings
  {:flex            1
   :flexDirection   :column
   :backgroundColor color-white})

(def body
  {:flex          1
   :flexDirection :column})

(def chat-name-text
  {:marginTop    24
   :marginLeft   16
   :marginBottom 16
   :color        text2-color
   :fontSize     14
   :lineHeight   20})

(defn chat-name-value-container [focused?]
  {:flexDirection     :row
   :marginLeft        16
   :height            56
   :alignItems        :center
   :justifyContent    :center
   :borderBottomWidth 2
   :borderBottomColor (if focused? color-purple separator-color)})

(def chat-name-value
  {:flex       1
   :fontSize   16
   :color      text1-color})

(def chat-name-validation-message
  {:marginTop  8
   :marginLeft 16
   :color      :red})

(defn chat-name-btn-edit-container [enabled?]
  {:padding        16
   :justifyContent :center
   :opacity        (if enabled? 1 0.3)})

(def chat-name-btn-edit-text
  {:color      text2-color
   :fontSize   16
   :lineHeight 20})

(def members-text
  {:marginTop    24
   :marginLeft   16
   :marginBottom 16
   :color        text2-color
   :fontSize     14
   :lineHeight   20})

(def add-members-icon
  {:marginVertical   -1
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
   :fontSize   16
   :lineHeight 20})

(def settings-text
  {:marginTop    24
   :marginLeft   16
   :marginBottom 16
   :color        text2-color
   :fontSize     14
   :lineHeight   20})

(def settings-container
  {:flexDirection :column})

(def setting-row
  {:flexDirection :row
   :height        56})

(def setting-icon-view
  {:width          56
   :height         56
   :alignItems     :center
   :justifyContent :center})

(def setting-view
  {:flex           1
   :marginLeft     16
   :alignItems     :flex-start
   :justifyContent :center})

(def setting-title
  {:marginTop  -2.5
   :color      text1-color
   :fontSize   16})

(def setting-subtitle
  {:marginTop  1
   :color      text2-color
   :fontSize   12})

(defn chat-color-icon [color]
  {:borderRadius    12
   :width           24
   :height          24
   :backgroundColor color})
