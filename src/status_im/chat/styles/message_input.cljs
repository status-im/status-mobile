(ns status-im.chat.styles.message-input
  (:require [status-im.components.styles :refer [color-white
                                                 color-blue]]
            [status-im.chat.constants :refer [max-input-height
                                              min-input-height
                                              input-spacing-top
                                              input-spacing-bottom]]))

(def input-container
  {:flex-direction :column})

(defn input-view [content-height]
  {:flex-direction   :row
   :align-items      :center
   :justify-content  :center
   :height           (+ (min (max min-input-height content-height) max-input-height)
                        input-spacing-top
                        input-spacing-bottom)
   :background-color color-white})

(defn message-input-container [content-height]
  {:height           (min (max min-input-height content-height) max-input-height)
   :margin-top       input-spacing-top
   :margin-bottom    input-spacing-bottom
   :flex             1
   :flex-direction   "column"
   :margin-right     0})

(def send-wrapper
  {:margin-bottom   8
   :margin-right    10
   :width           36
   :flex            1
   :flex-direction  "column"
   :align-items     :center
   :justify-content :flex-end})

(def send-container
  {:width            36
   :height           36
   :border-radius    18
   :background-color color-blue})

(def send-icon
  {:margin-top  10.5
   :margin-left 12
   :width       15
   :height      15})