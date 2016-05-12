(ns syng-im.group-settings.styles.member
  (:require [syng-im.components.styles :refer [font
                                               title-font
                                               text1-color
                                               text2-color
                                               color-white
                                               online-color]]))

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

(def info-container
  {:flex           1
   :flexDirection  :column
   :marginLeft     16
   :justifyContent :center})

(def name-text
  {:marginTop  -2
   :fontSize   16
   :fontFamily font
   :color      text1-color})

(def role-text
  {:marginTop  1
   :fontSize   12
   :fontFamily font
   :color      text2-color})

(def more-btn
  {:width          56
   :height         56
   :alignItems     :center
   :justifyContent :center })

(def more-btn-icon
  {:width  4
   :height 16})
