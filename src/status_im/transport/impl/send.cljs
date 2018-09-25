(ns status-im.transport.impl.send
  (:require
   [status-im.group-chats.core :as group-chats]
   [status-im.transport.message.core :as message]
   [status-im.transport.message.v1.core :as transport]))

(extend-type transport/GroupMembershipUpdate
  message/StatusMessage
  (send [this chat-id cofx]
    (group-chats/send-membership-update this chat-id cofx)))

(extend-type transport/GroupLeave
  message/StatusMessage
  (send [this chat-id cofx]
    (group-chats/handle-group-leave this chat-id cofx)))
