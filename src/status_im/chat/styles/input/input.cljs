(ns status-im.chat.styles.input.input
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def min-input-height 36)
(def padding-vertical 8)
(def border-height 1)
(def max-input-height (* 4 min-input-height))

(defnstyle root [margin-bottom]
  {:background-color colors/white
   :margin-bottom    margin-bottom
   :flex-direction   :column
   :border-top-width border-height
   :border-top-color colors/gray-light
   :elevation        2})

(def input-container
  {:flex-direction   :row
   :align-items      :flex-end
   :padding-left     14})

(def input-root
  {:padding-top    padding-vertical
   :padding-bottom padding-vertical
   :flex           1})

(defn input-animated [content-height]
  {:align-items      :flex-start
   :flex-direction   :row
   :flex-grow        1
   :height           (min (max min-input-height content-height) max-input-height)})

(defnstyle input-view [content-height single-line-input?]
  {:flex           1
   :font-size      15
   :padding-top    9
   :padding-bottom 5
   :padding-right  12
   :height         (if single-line-input?
                     min-input-height
                     (+ (min (max min-input-height content-height) max-input-height)))
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
   :ios                 {:line-height 43}})

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
