(ns syng-im.components.chat.suggestions-styles
  (:require [syng-im.components.styles :refer [font
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

(def suggestion-height 88)

(def suggestion-container
  {:flexDirection   :column
   :paddingLeft     16
   :backgroundColor color-white})

(def suggestion-sub-container
  {:height            suggestion-height
   :borderBottomWidth 1
   :borderBottomColor separator-color})

(defn suggestion-background
  [{:keys [color]}]
  {:alignSelf       :flex-start
   :marginTop       10
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
   :height           (min 168 (* suggestion-height suggestions-count))
   :backgroundColor  color-white
   :borderRadius     5})

(def drag-down-touchable
  {:height         22
   :alignItems     :center
   :justifyContent :center})

(def drag-down-icon
  {:width  16
   :height 16})
