(ns status-im2.contexts.chat.home.chat-list-item.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  []
  {:margin-top         8
   :margin-horizontal  8
   :padding-vertical   8
   :padding-horizontal 12
   :border-radius      12
   :flex-direction     :row
   :align-items        :center})

(defn count-container
  []
  {:width            8
   :height           8
   :border-radius    4
   :position         :absolute
   :right            26
   :top              16
   :background-color (colors/theme-colors colors/neutral-40 colors/neutral-60)})

(def muted-icon {:position         :absolute
                 :right            26
                 :top              16})

(defn timestamp
  []
  {:color       (colors/theme-colors colors/neutral-50 colors/neutral-40)
   :margin-top  3
   :margin-left 8})
