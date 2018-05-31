(ns status-im.ui.screens.add-new.new-public-chat.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def group-chat-name-input
  {:font-size      17
   :padding-bottom 0
   :letter-spacing -0.2
   :color          colors/text})

(def topic-hash
  (merge group-chat-name-input
         {:margin-left 14}))

(def group-container
  {:flex           1
   :flex-direction :column})

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

(def input-container
  {:padding          0
   :padding-right    16
   :background-color nil})

(def tooltip
  {:bottom-value -15
   :color        colors/red-light
   :font-size    12})
