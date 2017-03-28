(ns status-im.chat.styles.input.input
  (:require [status-im.components.styles :as common]
            [status-im.utils.platform :as platform]))

(def color-root-border "rgba(192, 198, 202, 0.28)")
(def color-input "#edf1f3")
(def color-input-helper-text "rgb(182, 189, 194)")
(def color-command "#70777d")
(def color-send "rgb(98, 143, 227)")

(def max-input-height 66)
(def min-input-height 38)
(def input-spacing-top 5)
(def input-spacing-bottom 8)

(defn root [margin-bottom]
  {:border-top-color color-root-border
   :border-top-width 1
   :flex-direction   :column
   :elevation        2
   :margin-bottom    margin-bottom})

(defn container [container-anim-margin]
  {:background-color common/color-white
   :flex-direction   :column
   :padding-left     container-anim-margin
   :padding-right    container-anim-margin
   :padding-top      8
   :padding-bottom   container-anim-margin})

(def input-container
  {:flex-direction :row
   :align-items    :flex-end})

(defn input-root [content-height anim-margin]
  {:align-items      :flex-start
   :background-color color-input
   :border-radius    8
   :flex-direction   :row
   :flex-grow        1
   :height           (+ (min (max min-input-height content-height) max-input-height) 0)
   :margin-top       anim-margin
   :padding-left     10
   :padding-right    10})

(def input-view
  {:flex             1
   :font-size        14
   :padding-top      input-spacing-top
   :padding-bottom   input-spacing-bottom
   :line-height      20})

(def invisible-input-text
  {:font-size        14
   :position         :absolute
   :left             0
   :background-color :transparent
   :color            :transparent})

(defn input-helper-text [left]
  (merge {:color               color-input-helper-text
          :font-size           14
          :position            :absolute
          :text-align-vertical :center
          :height              min-input-height}
         (if platform/android?
           {:left (+ 18 left)
            :top  -1}
           {:line-height min-input-height
            :left        (+ 14 left)})))

(def input-emoji-icon
  {:height 20
   :width  20})

(def input-clear-container
  {:width            24
   :height           24
   :margin-top       7
   :align-items      :center})

(def input-clear-icon
  {:width      12
   :height     12
   :margin-top 6})

(def commands-root
  {:flex-direction :row
   :align-items    :center})

(def command-list-icon-container
  {:width   32
   :height  32
   :padding 4})

(def commands-list-icon
  {:height 24
   :width  24})

(def send-message-container
  {:background-color color-send
   :width            38
   :height           38
   :border-radius    19
   :padding          7
   :margin-left      8})

(def send-message-icon
  {:height 24
   :width  24})

(def commands
  {:flex-direction :row
   :margin-left    16})

(def command
  {:color          color-command
   :font-size      14
   :margin-right   16
   :padding-top    8
   :padding-bottom 8})
