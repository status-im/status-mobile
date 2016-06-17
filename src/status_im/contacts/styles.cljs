(ns status-im.contacts.styles
  (:require [status-im.components.styles :refer [font
                                                 title-font
                                                 text1-color
                                                 color-white
                                                 toolbar-background2
                                                 online-color]]))



(def contacts-list-container
  {:flex            1
   :backgroundColor :white})

(def contacts-list
  {:backgroundColor :white})

(def contact-photo-container
  {:marginTop  4
   :marginLeft 12})

(def photo-image
  {:borderRadius 50
   :width        40
   :height       40})

(def online-container
  {:position        :absolute
   :top             24
   :left            24
   :width           20
   :height          20
   :borderRadius    50
   :backgroundColor online-color
   :borderWidth     2
   :borderColor     color-white})

(def contact-container
  {:flexDirection :row
   :height        56})

(def photo-container
  {:marginTop  8
   :marginLeft 16
   :width      44
   :height     44})

(def name-container
  {:marginLeft 12
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