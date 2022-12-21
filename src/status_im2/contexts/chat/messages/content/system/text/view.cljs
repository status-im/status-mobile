(ns status-im2.contexts.chat.messages.content.system.text.view
  (:require [react-native.core :as rn]
            [status-im.ui2.screens.chat.messages.message :as old-message]))

(defn text-content [message-data]
  [rn/view {:accessibility-label :chat-item}
   [old-message/render-parsed-text message-data]])
