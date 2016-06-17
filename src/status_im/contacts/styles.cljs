(ns status-im.contacts.styles
  (:require [status-im.components.styles :refer [font
                                                 title-font
                                                 text1-color
                                                 text3-color
                                                 color-white
                                                 toolbar-background2
                                                 online-color]]))

(def contacts-list-container
  {:flex            1
   :backgroundColor :white})

(def contacts-list
  {:backgroundColor :white})

(def letter-container
  {:paddingTop  11
   :paddingLeft 20
   :width       56})

(def letter-text
  {:fontSize   24
   :fontFamily font
   :color      text3-color})

(def contact-photo-container
  {:marginTop  4
   :marginLeft 12})

(def contact-container
  {:flexDirection :row
   :height        56})

(def name-container
  {:marginLeft     12
   :justifyContent :center})

(def name-text
  {:fontSize   16
   :fontFamily font
   :color      text1-color})

; new contact

(def contact-form-container
  {:flex  1
   :color :white})

(def gradient-background
  {:position :absolute
   :top      0
   :right    0
   :bottom   0
   :left     0})

(def form-container
  {:marginLeft 16
   :margin-top 50})