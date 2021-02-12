(ns status-im.chat.models.transport
  (:require [status-im.utils.fx :as fx]
            [status-im.transport.message.core :as transport.message]
            [status-im.chat.models.message :as chat.message]))

(fx/defn chat-ui-resend-message
  {:events [:chat.ui/resend-message]}
  [{:keys [db] :as cofx} chat-id message-id]
  (let [message (get-in db [:messages chat-id message-id])]
    (fx/merge
     cofx
     (transport.message/set-message-envelope-hash chat-id message-id (:message-type message) 1)
     (chat.message/resend-message chat-id message-id))))