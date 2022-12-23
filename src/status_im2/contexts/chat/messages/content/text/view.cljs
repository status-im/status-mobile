(ns status-im2.contexts.chat.messages.content.text.view
  (:require [status-im.ui2.screens.chat.messages.message :as old-message]))

(defn text-content [message-data]
  [old-message/render-parsed-text message-data])
