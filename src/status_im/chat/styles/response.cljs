(ns status-im.chat.styles.response
  (:require [status-im.components.styles :refer [font
                                                 color-white
                                                 color-blue
                                                 text1-color
                                                 text2-color
                                                 chat-background
                                                 color-black]]))

(def request-info-height 61)

(def drag-touchable
  {:height     16
   :alignItems :center})

(def drag-down-icon
  {:width   16
   :height  16
   :opacity 0.4})

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
  {:flex       1
   :marginLeft 12})

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

(defn request-view [height]
  {:flexDirection :column
   :height        height})

(defn request-info [color]
  {:flexDirection   :column
   :height          request-info-height
   :backgroundColor color})

(def inner-container
  {:flexDirection :row
   :height        45})

(def cancel-container
  {:marginTop   2.5
   :marginRight 16
   :width       24
   :height      24})

(def cancel-icon
  {:marginTop  6
   :marginLeft 6
   :width      12
   :height     12})

(def command-input
  {:flex       1
   :marginLeft 16
   :marginTop  -2
   :padding    0
   :fontSize   14
   :fontFamily font
   :color      text1-color})
