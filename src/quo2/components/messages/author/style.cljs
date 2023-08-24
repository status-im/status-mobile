(ns quo2.components.messages.author.style
  (:require [quo2.foundations.colors :as colors]))

(def container
  {:flex-wrap      :wrap
   :flex-direction :row
   :align-items    :center})

(defn middle-dot-nickname
  []
  {:color             (colors/theme-colors colors/neutral-40 colors/neutral-50)
   :margin-horizontal 4})

(defn chat-key-text
  []
  {:color       (colors/theme-colors colors/neutral-40 colors/neutral-50)
   :margin-left 8})

(defn middle-dot-chat-key
  []
  {:color       (colors/theme-colors colors/neutral-40 colors/neutral-50)
   :margin-left 4})

(def icon-container
  {:margin-left 4})

(defn time-text
  [verified?]
  {:color       (colors/theme-colors colors/neutral-40 colors/neutral-50)
   :padding-top 1
   :margin-left (if verified? 8 4)})
