(ns status-im.chat.styles.input
  (:require [status-im.components.styles :refer [color-white
                                                 color-blue
                                                 text1-color]]))

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

