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

(def message-row-column
  {:flex-direction :column})

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

(def chat-box-inner
  {:flex-direction  :row
   :flex            1})

(def chat-text-input
  {:flex 1})

(def messages-view
  {:flex             1
   :background-color colors/gray-lighter})

(def messages-scrollview-inner
  {:padding-vertical 46})

(def photo-style
  {:borderRadius 20
   :width        40
   :height       40
   :margin-right 8})

(def toolbar-chat-view
  {:align-items     :center
   :padding         11
   :justify-content :center})

(def toolbar-chat-name
  {:font-size   16
   :color       :black
   :font-weight "600"})

(def add-contact
  {:background-color :white
   :border-radius    6
   :margin-top       3
   :padding          4})

(def add-contact-text
  {:font-size 14
   :color     colors/gray})

(def message-text
  {:font-size 14})

(def message-wrapper
  {:flex-direction  :row
   :flex-wrap       :wrap})

(def not-first-in-group-wrapper
  {:flex-direction :row})

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