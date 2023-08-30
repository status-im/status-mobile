(ns status-im2.contexts.chat.messages.content.status.style
  (:require [quo2.foundations.colors :as colors]))

(def status-container
  {:flex-direction :row
   :align-items    :center})

(defn message-status-text
  []
  {:margin-left 4
   :color       (colors/theme-colors colors/neutral-40 colors/neutral-50)})
