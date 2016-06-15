(ns status-im.chat.styles.message-input
  (:require [status-im.components.styles :refer [color-white
                                                 color-blue]]))

(def input-height 56)

(defn message-input-container [offset]
  {:flex 1
   :transform [{:translateX offset}]
   :marginRight offset})

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
