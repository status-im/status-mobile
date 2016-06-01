(ns status-im.chat.styles.plain-input
  (:require [status-im.components.styles :refer [font
                                               text2-color
                                               color-white
                                               color-blue]]))

(def input-container
  {:flexDirection :column})

(def input-view
  {:flexDirection   :row
   :height          56
   :backgroundColor color-white})

(def message-input-button-touchable
  {:width          56
   :height         56
   :alignItems     :center
   :justifyContent :center})

(defn message-input-button [scale]
  {:transform [{:scale scale}]})

(defn message-input-container [offset]
  {:flex 1
   :transform [{:translateX offset}]
   :marginRight offset})

(def list-icon
  {:width  13
   :height 12})

(def close-icon
  {:width  12
   :height 12})

(def message-input
  {:flex       1
   :marginTop  -2
   :padding    0
   :fontSize   14
   :fontFamily font
   :color      text2-color})

(def smile-icon
  {:width       20
   :height      20})

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
