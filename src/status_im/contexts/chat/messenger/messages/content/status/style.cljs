(ns status-im.contexts.chat.messenger.messages.content.status.style
  (:require
    [quo.foundations.colors :as colors]))

(def status-container
  {:flex-direction :row
   :align-items    :center})

(defn message-status-text
  [theme]
  {:margin-left 4
   :color       (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)})
