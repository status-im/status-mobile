(ns status-im.transport.impl.send
  (:require [status-im.group-chats.core :as group-chats]
            [status-im.pairing.core :as pairing]
            [status-im.transport.message.pairing :as transport.pairing]
            [status-im.transport.message.group-chat :as transport.group-chat]
            [status-im.transport.message.protocol :as protocol]))

(extend-type transport.group-chat/GroupMembershipUpdate
  protocol/StatusMessage
  (send [this chat-id cofx]
    (group-chats/send-membership-update cofx this chat-id)))
