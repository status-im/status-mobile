(ns status-im2.contexts.chat.messages.content.system.text.view
  (:require
    [legacy.status-im.ui.screens.chat.message.legacy-view :as old-message]
    [react-native.core :as rn]))

(defn text-content
  [message-data]
  [rn/view {:accessibility-label :chat-item}
   [old-message/render-parsed-text message-data]])
