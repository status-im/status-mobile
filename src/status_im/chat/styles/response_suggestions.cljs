(ns status-im.chat.styles.response-suggestions
  (:require [status-im.components.styles :refer [font
                                                 font-medium
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

(def header-height 50)
(def suggestion-height 56)

(def header-container
  {:paddingLeft     16
   :height          header-height
   :backgroundColor color-white})

(def header-text
  {:marginTop  18
   :fontSize   13
   :fontFamily font-medium
   :color      text2-color})

(def suggestion-container
  {:flexDirection   :column
   :paddingLeft     16
   :height          56
   :backgroundColor color-white})

(def suggestion-sub-container
  {:height            suggestion-height
   :borderBottomWidth 1
   :borderBottomColor separator-color})

(def value-text
  {:marginTop  10
   :fontSize   12
   :fontFamily font
   :color      text1-color})

(def description-text
  {:marginTop  2
   :fontSize   12
   :fontFamily font
   :color      text2-color})

(defn suggestions-container [suggestions]
  {:flexDirection    :row
   :marginVertical   1
   :marginHorizontal 0
   :height           (min 150 (reduce + 0 (map #(if (:header %)
                                                 header-height
                                                 suggestion-height)
                                               suggestions)))
   :backgroundColor  color-white
   :borderRadius     5})
