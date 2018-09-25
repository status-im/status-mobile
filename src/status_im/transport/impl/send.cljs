(ns status-im.transport.impl.send
  (:require
   [status-im.group-chats.core :as group-chats]
   [status-im.transport.message.core :as message]
   [status-im.transport.message.v1.core :as transport.protocol]))

(extend-type transport.protocol/GroupMembershipUpdate
  message/StatusMessage
  (send [this chat-id cofx]
    (group-chats/send-membership-update this chat-id cofx)))
