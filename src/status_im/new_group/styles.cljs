(ns status-im.new-group.styles
  (:require [status-im.components.styles :refer [color-white
                                                 color-blue
                                                 text1-color
                                                 text2-color
                                                 toolbar-background1]]))

(defn toolbar-icon [enabled?]
  {:width   20
   :height  18
   :opacity (if enabled? 1 0.3)})

(def new-group-container
  {:flex             1
   :flex-direction   :column
   :background-color color-white})

(def chat-name-container
  {:margin-left 16})

(def group-chat-name-input
  {:font-size  14
   :color      text1-color})

(def group-chat-name-wrapper
  {:padding-top 0})

(def members-text
  {:margin-top    24
   :margin-bottom 8
   :color         text2-color
   :font-size     14
   :line-height   20})

(def add-container
  {:flex-direction :row
   :margin-bottom  16})

(def add-icon
  {:margin-vertical   18
   :margin-horizontal 3
   :width             17
   :height            17})

(def add-text
  {:margin-top  16
   :margin-left 32
   :color       text2-color
   :font-size   14
   :line-height 20})

(def contacts-list
  {:background-color :white})

(def contact-container
  {:flex-direction :row
   :justify-content :center
   :align-items :center
   :height         56})

(def contact-item-checkbox
  {:outer-size  20
   :filter-size 16
   :inner-size  12
   :outer-color color-blue
   :inner-color color-blue})
