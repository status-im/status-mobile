(ns status-im2.contexts.chat.messages.content.text.view
  (:require
    [react-native.core :as rn]
    [status-im.ui2.screens.chat.messages.message :as old-message]
    [status-im2.contexts.chat.messages.link-preview.view :as link-preview]))

(defn text-content
  [message-data context]
  [rn/view
   [old-message/render-parsed-text message-data]
   [link-preview/link-preview message-data context]])
