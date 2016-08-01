(ns status-im.components.drawer.styles
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

(def drawer-menu
  {:flex             1
   :background-color color-white
   :flex-direction   :column})

(def user-photo-container
  {:margin-top      40
   :align-items     :center
   :justify-content :center})

(def user-photo
  {:border-radius 32
   :width         64
   :height        64})

(def name-container
  {:margin-top   20
   :margin-left  16
   :margin-right 16
   :align-items  :center})

(def menu-items-container
  {:flex           1
   :margin-top     50
   :align-items    :stretch
   :flex-direction :column})

(def menu-item-touchable
  {:height      48
   :paddingLeft 16
   :paddingTop  14})

(def menu-item-text
  {:font-size   14
   :line-height 21
   :color       text1-color})

(def name-text
  {:color     text1-color
   :font-size 16})

(def switch-users-container
  {:padding-vertical 36
   :align-items      :center})

(def switch-users-text
  {:font-size   14
   :line-height 21
   :color       text3-color})
