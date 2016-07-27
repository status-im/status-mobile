(ns status-im.components.tabs.styles
  (:require [status-im.components.styles :refer [font
                                                 title-font
                                                 color-white
                                                 chat-background
                                                 online-color
                                                 selected-message-color
                                                 separator-color
                                                 text1-color
                                                 text2-color
                                                 toolbar-background1]]))

(def tabs-height 59)
(def tab-height 56)

(defn tabs-container [hidden? animation? offset-y]
  {:position        :absolute
   :bottom          0
   :left            0
   :right           0
   :height          tabs-height
   :backgroundColor color-white
   :marginBottom    (if (or hidden? animation?)
                      (- tabs-height) 0)
   :transform       [{:translateY (if animation? offset-y 1)}]})

(def bottom-gradient
  {:position :absolute
   :bottom 0
   :left 0
   :right 0
   :height 4})

(def tabs-inner-container
  {:flexDirection   :row
   :height          tab-height
   :opacity         1
   :backgroundColor :white
   :justifyContent  :center
   :alignItems      :center})

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
   :marginBottom 1})

(def tab-container
  {:flex           1
   :height         tab-height
   :justifyContent :center
   :alignItems     :center})

(defn tab-view-container [offset-x]
  {:position       :absolute
   :top            0
   :left           0
   :right          0
   :bottom         0
   :padding-bottom 59
   :transform      [{:translateX offset-x}]})
