(ns status-im.chat.styles.input.send-button
  (:require [status-im.ui.components.colors :as colors]))

(defn send-message-container [rotation]
  {:background-color colors/blue
   :width            30
   :height           30
   :border-radius    15
   :margin           10
   :padding          4
   :margin-left      8
   :margin-bottom    11
   :transform        [{:rotate rotation}]})

(def send-message-icon
  {:height 22
   :width  22})
