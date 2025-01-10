(ns status-im.contexts.chat.messenger.messages.content.pin.style
  (:require
    [quo.foundations.colors :as colors]))

(def pin-indicator-container
  {:padding-left   42
   :margin-bottom  2
   :align-items    :center
   :flex-direction :row})

(def pin-author-text
  {:color       colors/primary-50
   :margin-left 2})
