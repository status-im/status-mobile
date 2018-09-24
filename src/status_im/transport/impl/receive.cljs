(ns status-im.transport.impl.receive
  (:require
   [status-im.chat.models.group-chat :as models.group-chat]
   [status-im.models.contact :as models.contact]
   [status-im.transport.message.core :as message]
   [status-im.transport.message.v1.contact :as transport.contact]
   [status-im.transport.message.v1.group-chat :as transport.group-chat]))

(extend-type transport.group-chat/GroupAdminUpdate
  message/StatusMessage
  (receive [this chat-id signature _ cofx]
    (models.group-chat/handle-group-admin-update this chat-id signature cofx)))

(extend-type transport.group-chat/GroupChatCreate
  message/StatusMessage
  (receive [this _ signature _ cofx]
    (models.group-chat/handle-group-chat-create this signature cofx)))

(extend-type transport.group-chat/GroupLeave
  message/StatusMessage
  (receive [this chat-id signature _ cofx]
    (models.group-chat/handle-group-leave chat-id signature cofx)))

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
