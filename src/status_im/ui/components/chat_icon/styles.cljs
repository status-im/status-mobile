(ns status-im.ui.components.chat-icon.styles
  (:require [quo.design-system.colors :as colors]
            [status-im.ui.components.emoji-thumbnail.utils :as emoji-utils]))

(defn default-chat-icon [color]
  {:margin           0
   :width            40
   :height           40
   :align-items      :center
   :justify-content  :center
   :border-radius    20
   :background-color color})

(defn default-chat-icon-chat-list [color]
  (merge (default-chat-icon color)
         {:width         40
          :height        40
          :border-radius 20}))

(defn default-community-icon-chat-list [color]
  (merge (default-chat-icon color)
         {:width         48
          :height        48
          :border-radius 48}))

(defn default-token-icon-chat-list [color]
  (merge (default-chat-icon color)
         {:width         20
          :height        20
          :border-radius 20}))

(defn default-chat-icon-chat-toolbar [color]
  (merge (default-chat-icon color)
         {:width         36
          :height        36
          :border-radius 18}))

(defn default-chat-icon-profile [color size]
  (merge (default-chat-icon color)
         {:width         size
          :height        size
          :border-radius (/ size 2)}))

(defn default-chat-icon-text [size]
  {:color       colors/white-transparent-70-persist
   :font-weight "700"
   :font-size   (/ size 2)
   :line-height size})

(defn emoji-chat-icon-text [size]
  {:font-size   (emoji-utils/emoji-font-size size)
   :line-height size
   :margin-top  (emoji-utils/emoji-top-margin-for-vertical-alignment size)})  ;; Required for vertical alignment bug - Check function defination for more info

(def chat-icon
  {:margin        4
   :border-radius 20
   :width         40
   :height        40})

(def chat-icon-chat-list
  (merge chat-icon
         {:width  40
          :height 40
          :margin 0}))

(def community-icon-chat-list
  (merge chat-icon
         {:width  48
          :height 48
          :margin 0}))

(def token-icon-chat-list
  (merge chat-icon
         {:width  20
          :height 20
          :margin 0}))

(def chat-icon-chat-toolbar
  (merge chat-icon
         {:width  36
          :height 36
          :margin 0}))

(defn custom-size-icon [size]
  (merge chat-icon
         {:width  size
          :height size
          :margin 0}))

(def chat-icon-profile
  (merge chat-icon
         {:width         64
          :height        64
          :border-radius 32}))

(def container-chat-list
  {:width  40
   :height 40})

(def token-icon-container-chat-list
  {:width  20
   :height 20})

(def community-icon-container-chat-list
  {:width  48
   :height 48})

(defn container-list-size [size]
  {:width  size
   :height size})

(def container-chat-toolbar
  {:width  36
   :height 36})

(defn chat-icon-profile-edit []
  {:width            24
   :height           24
   :border-radius    12
   :border-width     1
   :border-color     colors/white-persist
   :background-color colors/blue
   :justify-content  :center
   :align-items      :center
   :position         :absolute
   :bottom           -2
   :right            -2})
