(ns status-im.ui.screens.desktop.main.chat.styles
  (:require [status-im.ui.components.colors :as colors]))

(defn message-box [{:keys [outgoing] :as message}]
  (let [align (if outgoing :flex-end :flex-start)
        color (if outgoing colors/hawkes-blue colors/white)]
    {:align-self       align
     :background-color color
     :border-radius    8
     :padding-left     12
     :padding-right    12
     :padding-top      10
     :padding-bottom   10
     :max-width        340}))

(defn message-row [{:keys [outgoing first-in-group?] :as message}]
  (let [padding-horizontal (if outgoing :padding-right :padding-left)
        padding-top-value (if first-in-group? 16 8)]
    {padding-horizontal 24
     :padding-top       padding-top-value}))

(defn message-timestamp-placeholder []
  {:color               colors/transparent
   :font-size           10
   :align-self          :flex-end
   :opacity             0.5
   :text-align          :right
   :text-align-vertical :center
   :width               60})

(defn message-timestamp []
  (merge (message-timestamp-placeholder)
         {:color    colors/gray
          :position :absolute
          :bottom   0
          :right    -5}))

(def author
  {:color         colors/gray
   :font-size     12
   :margin-left   48
   :margin-bottom 4})

(def chat-box
  {:height            68
   :background-color  :white
   :border-radius     12
   :margin-horizontal 24
   :padding-vertical  15})

(def chat-text-input
  {:flex 1})

(def messages-view
  {:flex             1
   :background-color colors/gray-lighter})

(def img-container
  {:height          56
   :justify-content :center})

(def photo-style
  {:border-radius 20
   :width        40
   :height       40
   :margin-right 8})

(def photo-style-toolbar
  {:border-radius 32
   :width         32
   :height        32
   :margin-right  8})

(defn topic-image [color]
  (merge photo-style-toolbar
         {:background-color color
          :align-items      :center
          :justify-content  :center}))

(def topic-text
  {:font-size 18
   :color     colors/white})

(def toolbar-chat-view
<<<<<<< a5a66a8d5e77e2b44ebc24e539ae20f50b4962a9
  {:margin-left     11
   :justify-content :center})
=======
  {:padding         11})
>>>>>>> navigate to user profile from chat

(def toolbar-chat-name
  {:font-size   16
   :color       :black
   :font-weight "300"})

(def add-contact
  {:background-color :white
   :border-radius    6
   :margin-top       3
   :padding-top      1})

(def add-contact-text
  {:font-size 14
   :color     colors/blue})

(def message-text
  {:font-size 14})

(def send-icon
  {:margin-left      16
   :width            30
   :height           30
   :border-radius    15
   :background-color colors/gray-lighter
   :align-items      :center
   :justify-content  :center
   :transform        [{:rotate "90deg"}]})

(def chat-view
  {:flex             1
   :background-color :white})

(defn contact-card-text [color]
  {:color     color
   :font-size 14})

(def contact-card-gray-text
  {:color     colors/gray
   :font-size 14})

(def chat-profile-body
  {:margin 32})

(def chat-profile-row
  {:flex-direction :row
   :align-items    :center
   :margin-bottom  10})

(def chat-profile-icon-container
  {:background-color (colors/alpha colors/blue 0.1)
   :justify-content  :center
   :align-items      :center
   :border-radius    15
   ;; not sure idiomatic reagent for multiple style values
   ;; for margin
   :width            22
   :height           22
   :margin-right     10})

(defn chat-profile-icon [color]
  {:tint-color color
   :width      15
   :height     15})

(def chat-profile-contact-code
  {:color         colors/gray
   :margin-top    10
   :margin-bottom 5})
