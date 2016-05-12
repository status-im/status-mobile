(ns syng-im.components.tabs.styles
  (:require [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               chat-background
                                               online-color
                                               selected-message-color
                                               separator-color
                                               text1-color
                                               text2-color
                                               toolbar-background1]]))

(def tab-height 56)

(def tabs
  {:flexDirection   :row
   :position        :absolute
   :bottom          0
   :right           0
   :left            0,
   :height          tab-height
   :opacity         1
   :backgroundColor :white
   :justifyContent  :center
   :alignItems      :center
   :borderTopColor  "#1c18354c"
   :borderTopWidth  1})

(def tab
  {:flex           1
   :height         tab-height
   :justifyContent :center
   :alignItems     :center})

(def tab-title
  {:fontFamily "sans-serif"
   :fontSize   14
   :color      "#6e93d8"})

(def tab-icon
  {:width        24
   :height       24
   :marginBottom 2})

(def tab-container
  {:flex           1
   :height         tab-height
   :justifyContent :center
   :alignItems     :center})