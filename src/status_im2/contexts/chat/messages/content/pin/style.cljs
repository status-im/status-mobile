(ns status-im2.contexts.chat.messages.content.pin.style
  (:require [quo2.foundations.colors :as colors]))

(def pin-indicator-container
  {:margin-top      4
   :margin-left     54
   :justify-content :center
   :align-self      :flex-start
   :align-items     :flex-start
   :flex-direction  :row})

(def pin-author-text
  {:color  colors/primary-50
   :bottom 2})

(def pinned-message-text
  {:color (colors/theme-colors colors/neutral-100 colors/white)})
