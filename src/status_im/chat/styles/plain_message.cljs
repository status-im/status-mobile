(ns status-im.chat.styles.plain-message
  (:require [status-im.components.styles :refer [text2-color]]))

(defn message-input-button-touchable [w]
  {:width          w
   :height         56
   :alignItems     :center
   :justifyContent :center})

(defn message-input-button [scale]
  {:transform [{:scale scale}]
   :width 24
   :height 24
   :alignItems     :center
   :justifyContent :center})

(def list-icon
  {:width  20
   :height 16})

(def requests-icon
  {:background-color :#7099e6
   :width            8
   :height           8
   :border-radius    8
   :left 0
   :top 0
   :position :absolute})

(def close-icon
  {:width  12
   :height 12})

(def message-input
  {:flex       1
   :marginTop  -2
   :padding    0
   :fontSize   14
   :color      text2-color})

(def smile-icon
  {:width       20
   :height      20})
