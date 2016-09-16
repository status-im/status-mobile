(ns status-im.accounts.login.styles
  (:require [status-im.components.styles :refer [text1-color
                                                 color-white
                                                 toolbar-background2
                                                 online-color]]))


(defn screen-container [height]
  {:height height})

(def gradient-background
  {:position :absolute
   :top      0
   :right    0
   :bottom   0
   :left     0})

(def form-container
  {:flex            1
   :flex-direction  "row"
   :align-items     "center"
   :justifyContent  "center"})

(def form-container-inner
  {:flex           1
   :padding-bottom 100
   :margin-left    16})

(def bottom-actions-container
  {:position :absolute
   :left     0
   :right    0
   :bottom   0})

(def connect-button-container
  {:flex 1})

(def connect-button
  {:backgroundColor   color-white
   :flex 1
   :alignItems :center
   :paddingVertical   16
   :paddingHorizontal 28})

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