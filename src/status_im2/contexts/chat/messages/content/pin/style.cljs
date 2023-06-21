(ns status-im2.contexts.chat.messages.content.pin.style
  (:require [quo2.foundations.colors :as colors]))

(def system-message-default-size 34)
(def system-message-margin-right 8)

(def pin-indicator-container
  {:padding-left   42
   :margin-bottom  2
   :align-items    :center
   :flex-direction :row})

(def pin-author-text
  {:color       colors/primary-50
   :margin-left 2})

(defn pinned-message-text
  []
  {:color       (colors/theme-colors colors/neutral-100 colors/white)
   :margin-left 4})

(def system-message-container
  {:flex-direction :row :margin-vertical 8})

(def system-message-inner-container
  {:width            system-message-default-size
   :height           system-message-default-size
   :border-radius    system-message-default-size
   :margin-right     system-message-margin-right
   :justify-content  :center
   :align-items      :center
   :background-color colors/primary-50-opa-10})

(def system-message-author-container
  {:flex-direction :row
   :align-items    :baseline})

(def system-message-timestamp-container
  {:padding-left 8
   :color        colors/neutral-50})
