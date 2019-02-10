(ns status-im.ui.screens.chat.styles.input.input
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def min-input-height 36)
(def padding-vertical 8)
(def border-height 1)
(def max-input-height (* 5 min-input-height))

(defnstyle root [margin-bottom]
  {:background-color colors/white
   :margin-bottom    margin-bottom
   :flex-direction   :column
   :border-top-width border-height
   :border-top-color colors/gray-light
   :elevation        2})

(def reply-message
  {:flex-direction :row
   :align-items    :flex-start
   :border-width   1
   :border-radius  10
   :border-color   colors/gray-light
   :padding-top    10
   :padding-bottom 10
   :padding-right  14
   :padding-left   7
   :margin-top     12
   :margin-left    12
   :margin-right   12})

(def reply-message-content
  {:flex-direction :column
   :padding-left   7
   :margin-right   30
   :max-height     140
   :overflow       :scroll})

(def reply-message-author
  {:font-size      12
   :color          colors/gray
   :padding-bottom 6})

(def reply-message-container
  {:flex-direction :column-reverse})

(def cancel-reply-highlight
  {:position :absolute
   :z-index  5
   :top      0
   :right    0
   :height   26})

(def cancel-reply-container
  {:flex-direction  :row
   :justify-content :flex-end
   :margin-right     12})

(def cancel-reply-icon
  {:background-color colors/gray
   :border-radius    12})

(def input-container
  {:flex-direction   :row
   :align-items      :flex-end
   :padding-left     14})

(def input-root
  {:padding-top    padding-vertical
   :padding-bottom padding-vertical
   :flex           1})

(def input-animated
  {:align-items    :flex-start
   :flex-direction :row
   :flex-grow      1
   :min-height     min-input-height
   :max-height     max-input-height})

(defnstyle input-view [single-line-input?]
  {:flex           1
   :font-size      15
   :padding-top    9
   :padding-bottom 5
   :padding-right  12
   :min-height     min-input-height
   :max-height     (if single-line-input?
                     min-input-height
                     max-input-height)
   :android        {:padding-top 3}})

(def invisible-input-text
  {:font-size        15
   :position         :absolute
   :left             0
   :background-color :transparent
   :color            :transparent})

(defnstyle invisible-input-text-height [container-width]
  {:width            container-width
   :flex             1
   :font-size        15
   :padding-top      5
   :padding-bottom   5
   :android          {:padding-top 3}
   :position         :absolute
   :left             0
   :background-color :transparent
   :color            :transparent})

(defnstyle input-helper-view [left opacity]
  {:opacity  opacity
   :position :absolute
   :height   min-input-height
   :android  {:left (+ 4 left)}
   :ios      {:left left}})

(defnstyle input-helper-text [left]
  {:color               colors/gray
   :font-size           15
   :text-align-vertical :center
   :flex                1
   :android             {:top -1}
   :ios                 {:line-height min-input-height}})

(defnstyle seq-input-text [left container-width]
  {:min-width           (- container-width left)
   :font-size           15
   :position            :absolute
   :text-align-vertical :center
   :align-items         :center
   :android             {:left   (+ 2 left)
                         :height (+ 2 min-input-height)
                         :top    0.5}
   :ios                 {:line-height min-input-height
                         :height      min-input-height
                         :left        left}})

(def input-commands-icon
  {:margin        14
   :height        24
   :width         24})

(def input-clear-container
  {:width       24
   :height      24
   :margin-top  7
   :align-items :center})

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
