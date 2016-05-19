(ns syng-im.components.chat-icon.styles
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

(defn default-chat-icon [color]
  {:margin          4
   :width           36
   :height          36
   :alignItems      :center
   :justifyContent  :center
   :borderRadius    50
   :backgroundColor color})

(defn default-chat-icon-chat-list [color]
  (merge (default-chat-icon color)
         {:width  40
          :height 40}))

(defn default-chat-icon-menu-item [color]
  (merge (default-chat-icon color)
         {:width  24
          :height 24}))

(def default-chat-icon-text
  {:marginTop  -2
   :color      color-white
   :fontFamily font
   :fontSize   16
   :lineHeight 20})

(def chat-icon
  {:margin       4
   :borderRadius 50
   :width        36
   :height       36})

(def chat-icon-chat-list
  (merge chat-icon
         {:width  40
          :height 40}))

(def chat-icon-menu-item
  (merge chat-icon
         {:width  24
          :height 24}))

(def online-view
  {:position        :absolute
   :bottom          0
   :right           0
   :width           20
   :height          20
   :borderRadius    50
   :backgroundColor online-color
   :borderWidth     2
   :borderColor     color-white})

(def online-view-menu-item
  (merge online-view
         {:width 15
          :height 15}))

(def online-dot
  {:position        :absolute
   :top             6
   :width           4
   :height          4
   :borderRadius    50
   :backgroundColor color-white})

(def online-dot-left (merge online-dot {:left 3}))
(def online-dot-right (merge online-dot {:left 9}))

(def online-dot-menu-item
  (merge online-dot
         {:top    4
          :width  3
          :height 3}))

(def online-dot-left-menu-item
  (merge online-dot-menu-item {:left 1.7}))
(def online-dot-right-menu-item
  (merge online-dot-menu-item {:left 6.3}))

(def container
  {:width  44
   :height 44})

(def container-chat-list
  {:width  48
   :height 48})

(def container-menu-item
  {:width  32
   :height 32})
