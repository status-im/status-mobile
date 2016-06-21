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
  {:borderRadius 50})

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

(def online-dot
  {:position        :absolute
   :top             6
   :width           4
   :height          4
   :borderRadius    50
   :backgroundColor color-white})

(def online-dot-left
  (assoc online-dot :left 3))

(def online-dot-right
  (assoc online-dot :left 9))

(def contact-container
  {:flexDirection :row
   :height        56})

(def photo-container
  {:marginTop  8
   :marginLeft 16
   :width      44
   :height     44})

(def name-container
  {:justifyContent :center})

(def name-text
  {:marginLeft 16
   :fontSize   16
   :fontFamily font
   :color      text1-color})

; new contact

(def contact-form-container
  {:flex  1
   :color :white
   :backgroundColor :white})

(def gradient-background
  {:position :absolute
   :top      0
   :right    0
   :bottom   0
   :left     0})

(def form-container
  {:marginLeft 16
   :margin-top 16})

(def address-explication-container
  {:flex 1
   :paddingLeft 16
   :paddingRight 16})

(def address-explication
  {:textAlign :center
   :color "#838c93de"})