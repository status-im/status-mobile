(ns status-im.chat.styles.message-input
  (:require [status-im.components.styles :refer [color-white
                                                 color-blue]]))

(def input-height 56)

(def message-input-container
  {:flex 1
   :marginRight 0})

(def input-container
  {:flexDirection :column})

(def input-view
  {:flexDirection   :row
   :height          input-height
   :backgroundColor color-white})

(def send-icon
  {:marginTop  10.5
   :marginLeft 12
   :width      15
   :height     15})

(def send-container
  {:marginTop       10
   :marginRight     10
   :width           36
   :height          36
   :borderRadius    50
   :backgroundColor color-blue})
