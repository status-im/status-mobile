(ns legacy.status-im.ui.components.chat-icon.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]))

(defn default-chat-icon
  [color]
  {:margin           0
   :width            40
   :height           40
   :align-items      :center
   :justify-content  :center
   :border-radius    20
   :background-color color})

(defn default-chat-icon-profile
  [color size]
  (merge (default-chat-icon color)
         {:width         size
          :height        size
          :border-radius (/ size 2)}))

(defn default-chat-icon-text
  [size]
  {:color       colors/white-transparent-70-persist
   :font-weight "700"
   :font-size   (/ size 2)
   :line-height size})

(def chat-icon
  {:margin        4
   :border-radius 20
   :width         40
   :height        40})

(defn custom-size-icon
  [size]
  (merge chat-icon
         {:width  size
          :height size
          :margin 0}))

(def container-chat-list
  {:width  40
   :height 40})

(defn container-list-size
  [size]
  {:width  size
   :height size})
