(ns status-im.ui.components.chat-icon.styles-redesign
  (:require [quo.design-system.colors :as colors]))

(defn default-chat-icon [color size]
  {:margin           0
   :width            size
   :height           size
   :align-items      :center
   :justify-content  :center
   :border-radius    (/ size 2)
   :background-color color})

(defn default-list-chat-icon [color size]
  (merge (default-chat-icon color size)
         {:width         size
          :height        size
          :border-radius (/ size 2)}))

(defn default-chat-icon-text [size]
  {:color       colors/white-transparent-70-persist
   :font-weight "700"
   :font-size   (/ size 2)
   :line-height size})

(def chat-icon
  {:margin        4
   :border-radius 24
   :width         48
   :height        48})

(def community-status-icon
  {:margin        4
   :border-radius 10
   :width         20
   :height        20})

(def community-icon-chat-list
  (merge chat-icon
         {:width  48
          :height 48
          :margin 0}))

(def community-status-chat-list-icon
  (merge community-status-icon
         {:width  20
          :height 20
          :margin 0}))

