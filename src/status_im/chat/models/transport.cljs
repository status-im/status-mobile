;;this ns is needed because of cycled deps
(ns status-im.chat.models.transport
  (:require [status-im.chat.models.message :as chat.message]
            [status-im.transport.message.core :as transport.message]
            [utils.re-frame :as rf]))

(rf/defn chat-ui-resend-message
  {:events [:chat.ui/resend-message]}
  [{:keys [db] :as cofx} chat-id message-id]
  (let [message (get-in db [:messages chat-id message-id])]
    (rf/merge
     cofx
     (transport.message/set-message-envelope-hash chat-id message-id (:message-type message))
     (chat.message/resend-message chat-id message-id))))
