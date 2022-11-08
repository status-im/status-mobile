(ns quo2.components.messages.author.style
  (:require [quo2.foundations.colors :as colors]))

(def container
  {:flex           1
   :flex-wrap      :wrap
   :height         18
   :flex-direction :row
   :align-items    :center})

(defn ens-text []
  {:color (colors/theme-colors colors/neutral-100 colors/white)})

(defn nickname-text []
  {:color (colors/theme-colors colors/neutral-100 colors/white)})

(def middle-dot-nickname
  {:color             colors/neutral-50
   :margin-horizontal 4})

(def chat-key-text
  {:color       colors/neutral-50
   :margin-left 8})

(def middle-dot-chat-key
  {:color       colors/neutral-50
   :margin-left 4})

(defn profile-name-text [nickname?]
  {:color (if nickname?
            (colors/theme-colors colors/neutral-60 colors/neutral-40)
            (colors/theme-colors colors/neutral-100 colors/white))})

(def icon-container
  {:margin-left 4})

(defn time-text [ens?]
  {:color       colors/neutral-50
   :margin-left (if ens? 8 4)})