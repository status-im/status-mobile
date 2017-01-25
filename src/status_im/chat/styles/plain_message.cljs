(ns status-im.chat.styles.plain-message
  (:require [status-im.components.styles :refer [text1-color
                                                 color-blue]]
            [status-im.chat.constants :refer [max-input-height
                                              min-input-height]]))

(defn message-input-button-touchable [width]
  {:width width
   :flex  1})

(defn message-input-button [scale margin-top]
  {:transform        [{:scale scale}]
   :width            24
   :height           24
   :margin-top       margin-top
   :margin-left      16
   :align-items      :center
   :justify-content  :center})

(def list-icon
  {:margin-left   4
   :margin-top    5.5
   :margin-bottom 5.5
   :margin-right  4
   :width         16
   :height        13})

(def requests-icon-container
  {:width            12
   :height           12
   :border-radius    12
   :left             -1
   :top              -1
   :background-color :white
   :position         :absolute})

(def requests-icon
  {:background-color color-blue
   :margin           2
   :width            8
   :height           8
   :border-radius    8})

(def close-icon
  {:width  12
   :height 12})

(def message-input
  {:flex        1
   :padding     0
   :font-size   14
   :line-height 20
   :color       text1-color
   :padding-top 0})

(def smile-icon
  {:width  20
   :height 20})
