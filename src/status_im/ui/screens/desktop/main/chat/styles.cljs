(ns status-im.ui.screens.desktop.main.chat.styles
  (:require [status-im.ui.components.colors :as colors]))

(def min-input-container-height 68)
(def max-input-container-height 180)
(def chat-vertical-padding 16)
(def min-input-area-height 20)
(def max-input-area-height (- max-input-container-height (* 2 chat-vertical-padding)))

(defn message-row [{:keys [outgoing first-in-group?] :as message}]
  (let [padding-horizontal (if outgoing :padding-right :padding-left)
        padding-top-value (if first-in-group? 16 8)]
    {padding-horizontal 24
     :padding-top       padding-top-value}))

(def message-row-column
  {:flex-direction :column})

(def message-timestamp
  {:color        colors/gray
   :font-size    10
   :text-align   :right
   :margin-right 16
   :width        60})

(def message-command-container
  {:align-self         :flex-start
   :border-radius      8
   :border-color       colors/gray-light
   :border-width       1
   :padding-horizontal 12
   :padding-vertical   10
   :align-items        :flex-start
   :width              230})

(def author
  {:color         colors/black
   :font-weight   :bold
   :font-size     14})

(defn chat-box [height]
  {:height            (+ height (* 2 chat-vertical-padding))
   :min-height        min-input-container-height
   :max-height        max-input-container-height
   :padding-vertical  chat-vertical-padding
   :flex-direction    :row
   :overflow          :hidden})

(defn chat-text-input [container-height]
  {:height            container-height
   :min-height        min-input-area-height
   :max-height        max-input-area-height
   :margin-left       20
   :margin-right      22
   :flex              1
   :align-self       :center})

(def messages-view
  {:flex             1
   :background-color colors/white})

(def img-container
  {:height          56
   :justify-content :center})

(def messages-list-vertical-padding 46)

(def photo-style
  {:border-radius 19
   :width         38
   :height        38})

(def member-photo-container
  {:border-color      colors/gray-light
   :border-width      1
   :align-items       :center
   :justify-content   :center
   :border-radius     20
   :width             40
   :height            40})

(def chat-icon
  {:width         34
   :border-radius 34
   :height        34
   :margin-right  12})

(defn topic-image [color]
  (merge chat-icon
         {:background-color color
          :align-items      :center
          :justify-content  :center}))

(def topic-text
  {:font-size 18.7
   :color     colors/white})

(def toolbar-chat-view
  {:align-items        :center
   :padding-vertical   17
   :padding-horizontal 24
   :height             68
   :flex-direction     :row
   :justify-content    :space-between})

(def add-contact-text
  {:font-size 12
   :color     colors/blue})

(def public-chat-text
  {:font-size 12
   :color colors/gray})

(defn profile-actions-text [color]
  {:font-size 12
   :color color
   :margin-bottom 4})

(defn message-text [outgoing]
  {:color (if outgoing colors/white colors/black)
   :font-size 14})

(defn message-link [outgoing]
  (assoc (message-text outgoing)
         :color (if outgoing colors/white colors/blue)
         :text-decoration-line :underline))

(def system-message-text
  {:color colors/black
   :margin-top -5
   :font-size 14})

(def message-container
  {:flex-direction :column
   :margin-right   16})

(def message-wrapper
  {:flex-direction  :row
   :flex-wrap       :wrap})

(def not-first-in-group-wrapper
  {:flex-direction :row})

(def send-button
  {:height           34
   :width            34
   :margin-right     24
   :justify-content  :center
   :align-items      :center
   :align-self       :flex-end})

(defn send-icon [not-active?]
  {:height           34
   :width            34
   :border-radius    17
   :background-color (if not-active? colors/gray-lighter colors/blue)
   :align-items      :center
   :justify-content  :center
   :transform        [{:rotate "90deg"}]})

(defn send-icon-arrow [not-active?]
  {:tint-color (if not-active? :gray :white)})

(def chat-view
  {:flex             1
   :flex-direction   :column
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

(defn chat-title-and-type [pending?]
  {:flex 1
   :justify-content (if pending? :flex-start :center)})

(def chat-title
  {:margin-bottom  4
   :font-size      14
   :color          colors/black})

(def reply-wrapper
  {:flex-direction :column-reverse})

(def reply-photo-style
  {:width         40
   :height        40
   :margin-right  5})

(def reply-container
  {:flex-direction   :row
   :align-items      :flex-start
   :border-width     1
   :border-radius    10
   :border-color     colors/gray-light
   :margin           10})

(def reply-content-container
  {:flex-direction :column
   :padding-bottom 10})

(def reply-content-author
  {:margin-top     5
   :color          colors/gray
   :font-size      12
   :padding-bottom 3})

(def reply-content-message
  {:padding-left   7
   :margin-right   50
   :max-height     140
   :overflow       :scroll})

(def reply-close-highlight
  {:position :absolute
   :z-index  5
   :top      3
   :right    8
   :height   26})

(def reply-close-icon
  {:border-radius     12
   :background-color  colors/gray
   :tint-color        colors/white})

(defn reply-icon [outgoing]
  {:tint-color (if outgoing colors/white colors/gray)})

(def separator
  {:height            1
   :background-color  colors/gray-light})

(def quoted-message-container
  {:margin        6
   :margin-left   0
   :padding       6
   :border-color  colors/gray-lighter
   :border-width  1
   :border-radius 8})

(def quoted-message-author-container
  {:flex-direction  :row
   :align-items     :flex-start})
