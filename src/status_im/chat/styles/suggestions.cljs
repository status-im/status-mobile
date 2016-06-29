(ns status-im.chat.styles.suggestions
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

(def suggestion-height 60)

(def suggestion-highlight
  {:margin-left 57})

(def suggestion-container
  {:backgroundColor color-white})

(def suggestion-sub-container
  {:height            suggestion-height
   :borderBottomWidth 1
   :borderBottomColor separator-color
   :flex-direction :row})

(defn suggestion-background
  [{:keys [color]}]
  {:marginTop       10
   :height          24
   :backgroundColor color
   :borderRadius    50})

(def suggestion-text
  {:marginTop        2.5
   :marginHorizontal 12
   :fontSize         12
   :fontFamily       font
   :color            color-white})

(def value-text
  {:marginTop  6
   :fontSize   14
   :fontFamily font
   :color      text1-color})

(def description-text
  {:marginTop  2
   :fontSize   12
   :fontFamily font
   :color      text2-color})

(defn suggestions-container [suggestions-count]
  {:flexDirection    :row
   :marginVertical   1
   :marginHorizontal 0
   :flex 1
   :height           (min 168 (* suggestion-height suggestions-count))
   :backgroundColor  color-white
   :borderRadius     5})

(defn container [height]
  {:flexDirection   :column
   :position        :absolute
   :left            0
   :right           0
   :bottom          0
   :height          height
   :backgroundColor color-white
   :elevation       2})
