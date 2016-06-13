(ns status-im.login.styles
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
   :bottom   16})

(def recover-text-container
  {:flex     1
   :flexWrap :nowrap
   :padding  16})

(def recover-text
  {:flex  1
   :color color-white})

(def connect-button-container
  {:position :absolute
   :right    16
   :top      0})

(def connect-button
  {:backgroundColor   color-white
   :borderRadius      55
   :paddingVertical   16
   :paddingHorizontal 28
   })

(def connect-button-text
  {:color    "#7099e6"
   :fontSize 16})