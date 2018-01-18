(ns status-im.chat.styles.input.input
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as common]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]))

(def color-root-border "#e8eaeb")
(def color-input "#edf1f3")
(def color-input-helper-text "rgb(182, 189, 194)")
(def color-input-helper-placeholder "rgb(182, 189, 194)")
(def color-command "#70777d")
(def color-send "rgb(98, 143, 227)")

(def min-input-height 38)
(def max-input-height (* 4 min-input-height))

(defnstyle root [margin-bottom]
  {:flex-direction   :column
   :elevation        2
   :margin-bottom    margin-bottom
   :border-top-width 1
   :border-top-color color-root-border})

(defn container [container-anim-margin bottom-anim-margin]
  {:background-color common/color-white
   :flex-direction   :column
   :padding-left     container-anim-margin
   :padding-right    container-anim-margin
   :padding-top      8
   :padding-bottom   bottom-anim-margin})

(defstyle input-container-view
  {:ios {:z-index 1}})

(def input-container
  {:flex-direction :row
   :align-items    :flex-end})

(defn input-root [content-height anim-margin]
  {:align-items      :flex-start
   :background-color color-input
   :flex-direction   :row
   :flex-grow        1
   :height           (min (max min-input-height content-height) max-input-height)
   :margin-top       anim-margin
   :padding-left     10
   :padding-right    10
   :border-radius    8})

(defnstyle input-touch-handler-view [container-width]
  {:position :absolute
   :width    container-width
   :height   min-input-height})

(defnstyle input-view [content-height single-line-input?]
  {:flex           1
   :font-size      14
   :padding-top    9
   :padding-bottom 5
   :height         (if single-line-input?
                     min-input-height
                     (+ (min (max min-input-height content-height) max-input-height)))
   :android        {:padding-top 3}})

(def invisible-input-text
  {:font-size        14
   :position         :absolute
   :left             0
   :background-color :transparent
   :color            :transparent})

(defnstyle invisible-input-text-height [container-width]
  {:width            container-width
   :flex             1
   :font-size        14
   :padding-top      5
   :padding-bottom   5
   :android          {:padding-top 3}
   :position         :absolute
   :left             0
   :background-color :transparent
   :color            :transparent})

(defnstyle input-helper-text [left]
  {:color               color-input-helper-text
   :font-size           14
   :position            :absolute
   :text-align-vertical :center
   :height              min-input-height
   :align-items         :center
   :android             {:left (+ 15 left)
                         :top  -1}
   :ios                 {:line-height min-input-height
                         :left        (+ 10 left)}})

(defnstyle seq-input-text [left container-width]
  {:min-width           (- container-width left)
   :font-size           14
   :position            :absolute
   :text-align-vertical :center
   :height              min-input-height
   :align-items         :center
   :android             {:left (+ 10 left)
                         :top  0.5}
   :ios                 {:line-height min-input-height
                         :left        (+ 9 left)}})

(def input-emoji-icon
  {:margin-top 7
   :height 24
   :width  24})

(def input-clear-container
  {:width            24
   :height           24
   :margin-top       7
   :align-items      :center})

(def input-clear-icon
  {:width      24
   :height     24
   :margin-top 0})

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

(def close-commands-list-icon
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
   :margin-right   16})

(defn command [first?]
  {:color          color-command
   :font-size      14
   :margin-left    (if first? 10 16)
   :padding-top    8
   :padding-bottom 8})
