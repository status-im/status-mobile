(ns status-im.new-group.styles
  (:require [status-im.components.styles :refer [color-white
                                                 color-purple
                                                 text1-color
                                                 text2-color
                                                 toolbar-background1]]))

(defn toolbar-icon [enabled?]
  {:width   20
   :height  18
   :opacity (if enabled? 1 0.3)})

(def new-group-container
  {:flex            1
   :flexDirection   :column
   :backgroundColor color-white})

(def chat-name-container
  {:marginHorizontal 16})

(def chat-name-text
  {:marginTop    24
   :marginBottom 16
   :color        text2-color
   :fontSize     14
   :lineHeight   20})

(def group-name-input
  {:marginLeft -4
   :fontSize   14
   :color      text1-color})

(def group-name-validation-message
  {:color :red})

(def members-text
  {:marginTop    24
   :marginBottom 16
   :color        text2-color
   :fontSize     14
   :lineHeight   20})

(def add-container
  {:flexDirection :row
   :marginBottom  16})

(def add-icon
  {:marginVertical   19
   :marginHorizontal 3
   :width            17
   :height           17})

(def add-text
  {:marginTop  18
   :marginLeft 32
   :color      text2-color
   :fontSize   14
   :lineHeight 20})

(def contacts-list
  {:backgroundColor :white})

(def contact-container
  {:flexDirection :row
   :height        56})
