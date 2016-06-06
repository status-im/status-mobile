(ns status-im.chat.styles.plain-message
  (:require [status-im.components.styles :refer [font
                                                 text2-color]]))

(def message-input-button-touchable
  {:width          56
   :height         56
   :alignItems     :center
   :justifyContent :center})

(defn message-input-button [scale]
  {:transform [{:scale scale}]})

(def list-icon
  {:width  13
   :height 12})

(def close-icon
  {:width  12
   :height 12})

(def message-input
  {:flex       1
   :marginTop  -2
   :padding    0
   :fontSize   14
   :fontFamily font
   :color      text2-color})

(def smile-icon
  {:width       20
   :height      20})
