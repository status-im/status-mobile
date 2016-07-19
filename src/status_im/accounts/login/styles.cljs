(ns status-im.accounts.login.styles
  (:require [status-im.components.styles :refer [font
                                                 title-font
                                                 text1-color
                                                 color-white
                                                 toolbar-background2
                                                 online-color]]))


(def screen-container
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
   :margin-top 70})

(def bottom-actions-container
  {:position :absolute
   :left     0
   :right    0
   :bottom   0})

(def recover-text-container
  {:flex     1
   :alignItems :center
   :padding  16})

(def recover-text
  {:flex  1
   :color color-white
   :fontSize 16})

(def connect-button-container
  {:flex 1})

(def connect-button
  {:backgroundColor   color-white
   :flex 1
   :alignItems :center
   :paddingVertical   16
   :paddingHorizontal 28
   })

(def connect-button-text
  {:color    "#7099e6"
   :fontSize 16})

(def input-style
  {:color :white
   :font-size 12})

(def scan-label
  {:color :white})

(def address-input-wrapper
  {})