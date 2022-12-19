(ns status-im2.contexts.chat.messages.content.text.view
  (:require [status-im.ui2.screens.chat.messages.message :as old-message]))

(defn text-content [message-data]
  ;; TODO (flexsurfer) refactor according to the new design and move code here
  [old-message/render-parsed-text message-data])
