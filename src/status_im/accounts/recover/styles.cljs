(ns status-im.accounts.recover.styles
  (:require [status-im.components.styles :refer [color-white]]))


(def screen-container
  {:flex             1
   :color            :white
   :background-color color-white})

(def gradient-background
  {:position :absolute
   :top      0
   :right    0
   :bottom   0
   :left     0})

(def recover-explain-container
  {:padding-horizontal 35
   :padding-top        20
   :justify-content    :center})

(def recover-explain-text
  {:color       "#838c93de"
   :font-size   16
   :line-height 20
   :text-align  :center})

(def form-container
  {:flex           1
   :flex-direction "row"
   :justifyContent "center"})

(def form-container-inner
  {:flex           1
   :padding-bottom 100
   :margin-left    16})

(def bottom-actions-container
  {:position :absolute
   :left     0
   :right    0
   :bottom   0})

(def recover-button-container
  {:flex 1})

(defn recover-button [valid-form?]
  {:backgroundColor   (if valid-form? "#7099e6" :gray)
   :color             :white
   :flex              1
   :alignItems        :center
   :paddingVertical   16
   :paddingHorizontal 28})

(def recover-button-text
  {:color    color-white
   :fontSize 16})

(def input-style
  {:color     "#323232"
   :font-size 12})

(def scan-label
  {:color :white})

(def address-input-wrapper
  {})