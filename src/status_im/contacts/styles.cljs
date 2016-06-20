(ns status-im.contacts.styles
  (:require [status-im.components.styles :refer [font
                                                 font-medium
                                                 title-font
                                                 text1-color
                                                 text2-color
                                                 text3-color
                                                 text5-color
                                                 color-white
                                                 toolbar-background2
                                                 online-color]]))

(def contacts-list-container
  {:flex            1
   :backgroundColor :white})

(def toolbar-shadow
  {:height          2
   :backgroundColor toolbar-background2})

(def contact-groups
  {:flex            1
   :backgroundColor toolbar-background2})

(def contacts-list
  {:backgroundColor :white})

(def contact-group
  {:flexDirection :column})

(def contact-group-header
  {:flexDirection   :column
   :backgroundColor toolbar-background2})

(def contact-group-header-inner
  {:flexDirection   :row
   :alignItems      :center
   :height          48
   :backgroundColor toolbar-background2})

(def contact-group-text
  {:flex       1
   :marginLeft 16
   :fontSize   14
   :fontFamily font-medium
   :color      text5-color})

(def contact-group-size-text
  {:marginRight 14
   :fontSize    12
   :fontFamily  font
   :color       text2-color})

(def contact-group-header-gradient-top
  {:flexDirection   :row
   :height          3
   :backgroundColor toolbar-background2})

(def contact-group-header-gradient-top-colors
  ["rgba(24, 52, 76, 0.165)"
   "rgba(24, 52, 76, 0.03)"
   "rgba(24, 52, 76, 0.01)"])

(def contact-group-header-gradient-bottom
  {:flexDirection   :row
   :height          2
   :backgroundColor toolbar-background2})

(def contact-group-header-gradient-bottom-colors
  ["rgba(24, 52, 76, 0.01)"
   "rgba(24, 52, 76, 0.05)"])

(def contact-group-header-height (+ (:height contact-group-header-inner)
                                    (:height contact-group-header-gradient-bottom)))

(def show-all
  {:flexDirection   :row
   :alignItems      :center
   :height          56
   :backgroundColor color-white})

(def show-all-text
  {:marginLeft    72
   :fontSize      14
   :fontFamily    font-medium
   :color         text3-color
   ;; ios only:
   :letterSpacing 0.5})

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
  {:flexDirection   :row
   :height          56
   :backgroundColor color-white})

(def name-container
  {:flex           1
   :marginLeft     12
   :justifyContent :center})

(def name-text
  {:fontSize   16
   :fontFamily font
   :color      text1-color})

(def more-btn
  {:width          56
   :height         56
   :alignItems     :center
   :justifyContent :center})

(def more-btn-icon
  {:width  4
   :height 16})

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