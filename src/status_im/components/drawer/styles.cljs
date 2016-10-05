(ns status-im.components.drawer.styles
  (:require [status-im.components.styles :refer [color-light-blue-transparent
                                                 color-white
                                                 color-black
                                                 color-blue
                                                 color-blue-transparent
                                                 selected-message-color
                                                 online-color
                                                 separator-color
                                                 text1-color
                                                 text2-color
                                                 text3-color
                                                 color-red]]))

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
  {:margin-top   -16
   :margin-bottom -16
   :margin-left  16
   :margin-right 16})

(def name-input-wrapper
  {})

(defn name-input-text [valid?]
  {:color      (if valid? text1-color
                          color-red)
   :text-align :center})

(def status-container
  {:margin-left  16
   :margin-right 16
   :margin-top   4
   :align-items  :center})

(def status-input
  {:align-self          "stretch"
   :height              56
   :font-size           14
   :padding-left        4
   :line-height         20
   :text-align          :center
   :text-align-vertical :top
   :color               text2-color})

(def menu-items-container
  {:flex           1
   :margin-top     20
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
