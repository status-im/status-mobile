(ns status-im.contexts.chat.messenger.messages.content.system.text.view
  (:require
    [legacy.status-im.ui.screens.chat.message.legacy-view :as old-message]
    [react-native.core :as rn]))

(defn text-content
  [message-data]
  [rn/view {:accessibility-label :chat-item :padding-horizontal 12 :padding-vertical 3}
   [old-message/render-parsed-text message-data]])
