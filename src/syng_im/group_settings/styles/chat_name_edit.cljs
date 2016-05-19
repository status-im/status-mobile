(ns syng-im.group-settings.styles.chat-name-edit
  (:require [syng-im.components.styles :refer [font
                                               color-white
                                               text1-color]]))

(def save-action-icon
  {:width  18
   :height 14})

(def chat-name-container
  {:flex            1
   :flexDirection   :column
   :backgroundColor color-white})

(def chat-name-input
  {:marginLeft 12
   :fontSize   14
   :fontFamily font
   :color      text1-color})
