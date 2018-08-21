(ns status-im.ui.screens.desktop.main.tabs.home.styles
  (:require [status-im.ui.components.colors :as colors]))

(def chat-list-view
  {:flex             1
   :background-color colors/white})

(defn chat-list-item [current?]
  {:padding-horizontal 16
   :flex-direction     :row
   :flex               1
   :justify-content    :space-between
   :background-color   (if current? colors/gray-lighter colors/white)
   :align-items        :center})

(def chat-list-header
  {:flex-direction :row
   :align-items    :center
   :height         68
   :padding        11})

(def img-container
  {:height          78
   :justify-content :center})

(def chat-icon
  {:width         46
   :height        46
   :border-radius 46
   :margin-right  16})

(def unread-messages-icon
  {:position         :absolute
   :width            22
   :height           22
   :border-radius    22
   :left             28
   :top              10
   :justify-content  :center
   :align-items      :center
   :background-color colors/blue})

(defn unread-messages-text [large?]
  {:color     colors/white
   :font-size (if large? 11 12)})

(def chat-list-separator
  {:height            1
   :background-color  colors/gray-light})

(def chat-name-box
  {:flex-direction :row
   :flex           1
   :align-items    :center})

(def chat-name-last-msg-box
  {:flex             1
   :padding-vertical 16})

(def chat-name
  {:font-size  14})

(def chat-last-message
  {:color     colors/gray
   :font-size 14})

(def timestamp
  {:justify-content :flex-start
   :align-items     :flex-end
   :padding-vertical 16})

(def add-new
  {:background-color colors/blue
   :width            34
   :height           34
   :border-radius    34
   :justify-content  :center
   :align-items      :center})

(defn topic-image [color]
  (merge chat-icon
         {:background-color color
          :align-items      :center
          :justify-content  :center}))

(def topic-text
  {:font-size 25.6
   :color     colors/white})
