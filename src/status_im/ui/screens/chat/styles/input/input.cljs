(ns status-im.ui.screens.chat.styles.input.input
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.chat.styles.message.message :refer [message-author-name]]))

(def min-input-height 36)
(def border-height 1)
(def max-input-height (* 5 min-input-height))

(defn root []
  {:background-color colors/white
   :flex-direction   :column
   :border-top-width border-height
   :border-top-color colors/gray-lighter})

(def reply-message
  {:flex-direction  :row
   :align-items     :flex-start
   :justify-content :space-between
   :padding-top     8
   :padding-bottom  8
   :padding-left    8})

(def reply-message-content
  {:flex-direction :column
   :padding-left   8
   :padding-right  8
   :max-height     140})

(defn reply-message-author [chosen?]
  (assoc (message-author-name chosen?)
         :flex-shrink 1
         ;; NOTE:  overriding the values from the definition of message-author-name
         :padding-left 0
         :padding-top 0
         :padding-bottom 0
         :margin 0
         :height 18
         :include-font-padding false))

(def reply-message-to-container
  {:flex-direction  :row
   :height          18
   :padding-top     0
   :padding-bottom  0
   :padding-right   8
   :justify-content :flex-start})

(def cancel-reply-highlight
  {:align-items     :center
   :width           44
   :height          44})

(def cancel-reply-icon
  {:background-color colors/gray
   :width            20
   :height           20
   :margin-top       4
   :align-items      :center
   :justify-content  :center
   :border-radius    10})

(def input-container
  {:flex-direction :row
   :align-items    :flex-end})

(def input-view
  {:flex               1
   :padding-top        12
   :padding-bottom     15
   :padding-horizontal 12
   :max-height         max-input-height})
