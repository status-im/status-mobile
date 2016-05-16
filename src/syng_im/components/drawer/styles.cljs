(ns syng-im.components.drawer.styles
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

(def user-photo
  {:borderRadius 50
   :width        64
   :height       64})

(def menu-item-touchable
  {:height      48
   :paddingLeft 16
   :paddingTop  14})

(def menu-item-text
  {:fontSize   14
   :fontFamily font
   :lineHeight 21
   :color      text1-color})

(def drawer-menu
  {:flex            1
   :backgroundColor color-white
   :flexDirection   :column})

(def user-photo-container
  {:marginTop      40
   :alignItems     :center
   :justifyContent :center})

(def name-container
  {:marginTop  20
   :alignItems :center})

(def name-text
  {:marginTop -2.5
   :color     text1-color
   :fontSize  16})

(def menu-items-container
  {:flex          1
   :marginTop     80
   :alignItems    :stretch
   :flexDirection :column})

(def switch-users-container
  {:paddingVertical 36
   :alignItems :center})

(def switch-users-text
  {:fontSize   14
   :fontFamily font
   :lineHeight 21
   :color      text3-color})
