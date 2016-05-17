(ns syng-im.profile.styles
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
                                               text2-color]]))

(def user-photo
  {:borderRadius 50
   :width        64
   :height       64})

(def user-online-container
  {:position        :absolute
   :top             44
   :left            44
   :width           24
   :height          24
   :borderRadius    50
   :backgroundColor online-color
   :borderWidth     2
   :borderColor     color-white})

(def user-online-dot
  {:position        :absolute
   :top             8
   :left            5
   :width           4
   :height          4
   :borderRadius    50
   :backgroundColor color-white})

(def user-online-dot-left
  (assoc user-online-dot :left 5))

(def user-online-dot-right
  (assoc user-online-dot :left 11))

(def profile-property-view-container
  {:height            85
   :paddingHorizontal 16})

(def profile-property-view-sub-container
  {:borderBottomWidth 1
   :borderBottomColor separator-color})

(def profile-property-view-label
  {:marginTop  16
   :fontSize   14
   :fontFamily font
   :color      text2-color})

(def profile-property-view-value
  {:marginTop  11
   :height     40
   :fontSize   16
   :fontFamily font
   :color      text1-color})

(def profile
  {:flex            1
   :backgroundColor color-white
   :flexDirection   :column})

(def back-btn-touchable
  {:position :absolute})

(def back-btn-container
  {:width  56
   :height 56})

(def back-btn-icon
  {:marginTop  21
   :marginLeft 23
   :width      8
   :height     14})

(def actions-btn-touchable
  {:position :absolute
   :right    0})

(def actions-btn-container
  {:width          56
   :height         56
   :alignItems     :center
   :justifyContent :center})

(def actions-btn-icon
  {:width  4
   :height 16})

(def status-block
  {:alignSelf  :center
   :alignItems :center
   :width      249})

(def user-photo-container
  {:marginTop 26})

(def user-name
  {:marginTop  20
   :fontSize   18
   :fontFamily font
   :color      text1-color})

(def status
  {:marginTop  10
   :fontFamily font
   :fontSize   14
   :lineHeight 20
   :textAlign  :center
   :color      text2-color})

(def btns-container
  {:marginTop     18
   :flexDirection :row})

(def message-btn
  {:height          40
   :justifyContent  :center
   :backgroundColor color-blue
   :paddingLeft     25
   :paddingRight    25
   :borderRadius    50})

(def message-btn-text
  {:marginTop  -2.5
   :fontSize   14
   :fontFamily font
   :color      color-white})

(def more-btn
  {:marginLeft      10
   :width           40
   :height          40
   :alignItems      :center
   :justifyContent  :center
   :backgroundColor color-blue-transparent
   :padding         8
   :borderRadius    50})

(def more-btn-image
  {:width  4
   :height 16})

(def profile-properties-container
  {:marginTop     20
   :alignItems    :stretch
   :flexDirection :column})

(def report-user-container
  {:marginTop       50
   :marginBottom    43
   :alignItems      :center})

(def report-user-text
  {:fontSize      14
   :fontFamily    font
   :lineHeight    21
   :color         text2-color
   ;; IOS:
   :letterSpacing 0.5})
