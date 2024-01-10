(ns status-im.contexts.chat.messenger.messages.content.lightbox.style
  (:require
    [quo.foundations.colors :as colors]))

(defn bottom-text
  [expandable-text?]
  {:color             colors/white
   :margin-horizontal 20
   :align-items       (when-not expandable-text? :center)
   :flex-grow         1})
