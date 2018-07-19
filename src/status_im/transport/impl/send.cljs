(ns status-im.transport.impl.send
  (:require
   [status-im.group-chats.core :as group-chats]
   [status-im.transport.message.core :as message]
   [status-im.transport.message.v1.core :as transport]))

(extend-type transport/GroupMembershipUpdate
  message/StatusMessage
  (send [this chat-id cofx]
    (group-chats/send-membership-update cofx this chat-id)))

(extend-type transport/GroupLeave
  message/StatusMessage
  (send [this chat-id cofx]
    (group-chats/send-group-leave this chat-id cofx)))
