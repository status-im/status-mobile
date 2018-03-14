(ns status-im.ui.screens.add-new.new-public-chat.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as styles]))

(def group-chat-name-input
  {:font-size      17
   :padding-bottom 0
   :letter-spacing -0.2
   :color          colors/text})

(defstyle group-chat-topic-input
  {:font-size      14
   :line-height    16
   :color          colors/text
   :padding-left   -1
   :ios            {:padding-bottom 0}})

(defstyle topic-hash-style
  {:width    10
   :position :absolute
   :android  {:top 8 :left 3}
   :ios      {:top 6 :left 3}})

(def topic-hash
  (merge group-chat-name-input
         {:margin-left 14}))

(def group-chat-name-wrapper
  {:flex 1})

(def group-container
  {:flex             1
   :flex-direction   :column})

(def chat-name-container
  {:margin-top 10})

(defstyle members-text
  {:color   colors/gray
   :ios     {:letter-spacing -0.2
             :font-size      16}
   :android {:font-size 14}})

(def section-title
  (merge members-text
         {:padding-horizontal 16}))

(def input-container
  {:flex-direction    :row
   :align-items       :center
   :border-radius     styles/border-radius
   :height            52
   :background-color  colors/gray-light
   :margin-top        24})

(def public-chat-icon
  {:background-color colors/blue
   :border-radius    50
   :width            40
   :height           40
   :align-items      :center
   :justify-content  :center})

(def public-chat-icon-symbol
  {:font-size 20
   :color     colors/white})
