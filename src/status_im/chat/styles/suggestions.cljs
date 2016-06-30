(ns status-im.chat.styles.suggestions
  (:require [status-im.components.styles :refer [font
                                                 color-light-blue-transparent
                                                 color-white
                                                 color-black
                                                 color-gray
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

(def command-description-container
  {:flex 0.6})

(def command-label-container
  {:flex 0.4
   :flex-direction :column
   :align-items :flex-end
   :margin-right 16})

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

(def title-container
  {:margin-left 57
   :margin-bottom 16})

(def title-text
  {:font-size 13
   :color :#8f838c93})

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

(defn container [height]
  {:flexDirection   :column
   :position        :absolute
   :left            0
   :right           0
   :bottom          0
   :height          height
   :backgroundColor color-white
   :elevation       2})

(def request-container
  {:height         56
   :flex-direction :row})

(def request-icon-container
  {:height         56
   :width          57
   :align-items    :center
   :justifyContent :center})

(defn request-icon-background [color]
  {:height           32
   :width            32
   :border-radius    32
   :background-color color
   :align-items      :center
   :justifyContent   :center})

(def request-icon
  {:width  12
   :height 12})

(def request-info-container
  {:height      56
   :padding-top 10})

(def request-info-description
  {:font-size 12
   :color     color-black})

(def request-message-info
  {:font-size  12
   :margin-top 2
   :color      color-gray})

(def header-icon
  {:background-color :#838c93
   :width 14
   :border-radius 1
   :height 3})
