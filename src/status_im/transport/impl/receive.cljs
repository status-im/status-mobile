(ns status-im.transport.impl.receive
  (:require
   [status-im.chat.models.group-chat :as models.group-chat]
   [status-im.models.contact :as models.contact]
   [status-im.group-chats.core :as group-chats]
   [status-im.transport.message.core :as message]
   [status-im.transport.message.v1.contact :as transport.contact]
   [status-im.transport.message.v1.core :as transport.protocol]))

(extend-type transport.protocol/GroupMembershipUpdate
  message/StatusMessage
  (receive [this _ signature _ cofx]
    (group-chats/handle-membership-update-received cofx this signature)))

(extend-type transport.protocol/GroupLeave
  message/StatusMessage
  (receive [this chat-id signature _ cofx]
    (models.group-chat/handle-group-leave cofx chat-id signature)))

(extend-type transport.contact/ContactRequest
  message/StatusMessage
  (receive [this _ signature timestamp cofx]
    (models.contact/receive-contact-request signature timestamp this cofx)))

(extend-type transport.contact/ContactRequestConfirmed
  message/StatusMessage
  (receive [this _ signature timestamp cofx]
    (models.contact/receive-contact-request-confirmation signature timestamp this cofx)))

(extend-type transport.contact/ContactUpdate
  message/StatusMessage
  (receive [this _ signature timestamp cofx]
    (models.contact/receive-contact-update signature timestamp this cofx)))
