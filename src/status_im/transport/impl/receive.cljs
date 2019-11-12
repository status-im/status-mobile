(ns status-im.transport.impl.receive
  (:require [status-im.group-chats.core :as group-chats]
            [status-im.contact.core :as contact]
            [status-im.utils.fx :as fx]
            [status-im.chat.models.message :as chat.message]
            [status-im.ens.core :as ens]
            [status-im.pairing.core :as pairing]
            [status-im.transport.message.contact :as transport.contact]
            [status-im.transport.message.group-chat :as transport.group-chat]
            [status-im.transport.message.pairing :as transport.pairing]
            [status-im.transport.message.core :as transport.message]

            [status-im.transport.message.protocol :as protocol]))

(extend-type transport.group-chat/GroupMembershipUpdate
  protocol/StatusMessage
  (receive [this _ signature timestamp {:keys [metadata js-obj] :as cofx}]
    (group-chats/handle-membership-update-received cofx this signature {:whisper-timestamp timestamp
                                                                        :metadata metadata})))

(extend-type transport.contact/ContactRequest
  protocol/StatusMessage
  (receive [this _ signature timestamp cofx]
    (fx/merge
     cofx
     (contact/handle-contact-update cofx signature timestamp this)
     (ens/verify-names-from-contact-request this signature))))

(extend-type transport.contact/ContactRequestConfirmed
  protocol/StatusMessage
  (receive [this _ signature timestamp cofx]
    (fx/merge
     cofx
     (contact/handle-contact-update cofx signature timestamp this)
     (ens/verify-names-from-contact-request this signature))))

(extend-type transport.contact/ContactUpdate
  protocol/StatusMessage
  (receive [this _ signature timestamp cofx]
    (fx/merge
     cofx
     (contact/handle-contact-update cofx signature timestamp this)
     (ens/verify-names-from-contact-request this signature))))

(extend-type transport.pairing/SyncInstallation
  protocol/StatusMessage
  (receive [this _ signature _ cofx]
    (pairing/handle-sync-installation cofx this signature)))

(extend-type transport.pairing/PairInstallation
  protocol/StatusMessage
  (receive [this _ signature timestamp cofx]
    (pairing/handle-pair-installation cofx this timestamp signature)))

(extend-type protocol/Message
  protocol/StatusMessage
  (receive [this chat-id signature timestamp {:keys [db] :as cofx}]
    (let [message (assoc (into {} this)
                         :message-id
                         (get-in cofx [:metadata :messageId])
                         :chat-id chat-id
                         :whisper-timestamp (* 1000 timestamp)
                         :alias (get-in cofx [:metadata :author :alias])
                         :identicon (get-in cofx [:metadata :author :identicon])
                         :from signature
                         :metadata (:metadata cofx))]
      (fx/merge cofx
                (chat.message/receive-one message)
                (ens/verify-names-from-message this signature)))))
