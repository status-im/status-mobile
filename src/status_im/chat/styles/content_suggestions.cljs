(ns status-im.chat.styles.content-suggestions
  (:require [status-im.components.styles :refer [font
                                                 color-light-blue-transparent
                                                 color-white
                                                 color-black
                                                 color-blue
                                                 color-blue-transparent
                                                 selected-message-color
                                                 online-color
                                                 separator-color
                                                 text1-color
                                                 text2-color
                                                 text3-color]]))

(def suggestion-height 56)

(def suggestion-container
  {:paddingLeft     16
   :backgroundColor color-white})

(def suggestion-sub-container
  {:height            suggestion-height
   :borderBottomWidth 1
   :borderBottomColor separator-color})

(def value-text
  {:marginTop  9
   :fontSize   14
   :fontFamily font
   :color      text1-color})

(def description-text
  {:marginTop  1.5
   :fontSize   14
   :fontFamily font
   :color      text2-color})

(defn suggestions-container [suggestions-count]
  {:flex 1
   :marginVertical   1
   :marginHorizontal 0
   :height           (min 150 (* suggestion-height suggestions-count))
   :backgroundColor  color-white
   :borderRadius     5})

(def container
  {:backgroundColor color-white})
