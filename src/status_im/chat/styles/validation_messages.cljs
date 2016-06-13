(ns status-im.chat.styles.validation-messages
  (:require [status-im.components.styles :refer [font
                                                 color-white
                                                 color-blue
                                                 text1-color
                                                 text2-color
                                                 chat-background
                                                 color-black
                                                 validation-message-background]]))

(def container-height 61)

(def drag-container
  {:height         16
   :alignItems     :center
   :justifyContent :center})

(def drag-icon
  {:width  14
   :height 3})

(def command-icon-container
  {:marginTop       1
   :marginLeft      12
   :width           32
   :height          32
   :alignItems      :center
   :justifyContent  :center
   :borderRadius    50
   :backgroundColor color-white})

(def command-icon
  {:width  9
   :height 15})

(def info-container
  {:flex   1
   :margin 12})

(def command-name
  {:marginTop  0
   :fontSize   12
   :fontFamily font
   :color      color-white})

(def message-info
  {:marginTop  1
   :fontSize   12
   :fontFamily font
   :opacity    0.69
   :color      color-white})

(def validation-messages
  {:flexDirection   :column
   :backgroundColor validation-message-background})

(def inner-container
  {:flexDirection :column})

(defn animated-container [height]
  {:flexDirection   :column
   :height          height
   :backgroundColor color-white
   :elevation       2})
