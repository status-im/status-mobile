(ns status-im2.contexts.chat.messages.content.system.text.view
  (:require [react-native.core :as rn]
            [status-im2.contexts.chat.messages.content.legacy-view :as old-message]))

(defn text-content
  [message-data]
  [rn/view {:accessibility-label :chat-item}
   [old-message/render-parsed-text message-data]])
