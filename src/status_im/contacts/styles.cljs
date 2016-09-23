(ns status-im.contacts.styles
  (:require [status-im.components.styles :refer [text1-color
                                                 text2-color
                                                 text3-color
                                                 text5-color
                                                 color-white
                                                 toolbar-background2
                                                 online-color
                                                 color-gray2]]))

(def contacts-list-container
  {:flex            1
   :backgroundColor :white})

(def toolbar-shadow
  {:height          2
   :backgroundColor toolbar-background2})

(def contact-groups
  {:flex            1
   :backgroundColor toolbar-background2})

(def empty-contact-groups
  (merge contact-groups
         {:align-items :center
          :padding-top 150}))

(def empty-contacts-icon
  {:height 62
   :width  62})

(def empty-contacts-text
  {:margin-top 12
   :font-size  16
   :color      color-gray2})

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
   :color      text5-color})

(def contact-group-size-text
  {:marginRight 14
   :fontSize    12
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
   :color         text3-color
   ;; ios only:
   :letterSpacing 0.5})

(def contact-container
  {:flexDirection   :row
   :backgroundColor color-white})

(def letter-container
  {:paddingTop  11
   :paddingLeft 20
   :width       56})

(def letter-text
  {:fontSize   24
   :color      text3-color})

(def contact-photo-container
  {:marginTop  4
   :marginLeft 12})

(def contact-inner-container
  {:flex            1
   :flexDirection   :row
   :height          56
   :backgroundColor color-white})

(def info-container
  {:flex           1
   :flexDirection  :column
   :marginLeft     12
   :justifyContent :center})

(def name-text
  {:fontSize   16
   :color      text1-color})

(def info-text
  {:marginTop  1
   :fontSize   12
   :color      text2-color})

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
   :color :white
   :backgroundColor :white})

(def gradient-background
  {:position :absolute
   :top      0
   :right    0
   :bottom   0
   :left     0})

(def form-container
  {:margin-left 16
   :margin-top  16})

(def address-explication-container
  {:flex 1
   :margin-top 30
   :paddingLeft 16
   :paddingRight 16})

(def address-explication
  {:textAlign :center
   :color "#838c93de"})

(def buttons-container
  {:position :absolute
   :bottom 0
   :right 0
   :width 200
   :height 170})

(def qr-input
  {:margin-right 42})
