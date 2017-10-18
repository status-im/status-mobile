(ns status-im.chat.new-public-chat.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.styles :as common]))

(def group-chat-name-input
  {:font-size      17
   :padding-bottom 0
   :letter-spacing -0.2
   :color          common/text1-color})

(defstyle group-chat-topic-input
  {:font-size      14
   :line-height    16
   :color          common/text1-color
   :padding-left   13
   :ios            {:padding-bottom 0}})

(defstyle topic-hash-style
  {:width    10
   :position :absolute
   :android  {:top 8 :left 3}
   :ios      {:top 6 :left 3}})

(def topic-hash
  (merge group-chat-name-input
         topic-hash-style))

(def group-chat-name-wrapper
  {:padding-top    0
   :height         40
   :padding-bottom 0})

(def group-container
  {:flex             1
   :flex-direction   :column
   :background-color common/color-white})

(def chat-name-container
  {:padding-left 16
   :margin-top   10})

(defstyle members-text
  {:color   common/color-gray4
   :ios     {:letter-spacing -0.2
             :font-size      16}
   :android {:font-size 14}})