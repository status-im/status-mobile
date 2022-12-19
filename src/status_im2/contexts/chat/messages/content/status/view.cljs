(ns status-im2.contexts.chat.messages.content.status.view
  (:require [status-im.ui2.screens.chat.messages.message :as old-message]))

;; TODO (flexsurfer) implement with quo2.components.info.info-message
(defn status [message-data]
  [old-message/message-status message-data])
