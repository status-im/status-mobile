(ns status-im.transport.impl.receive
  (:require [status-im.group-chats.core :as group-chats]
            [status-im.contact.core :as contact]
            [status-im.pairing.core :as pairing]
            [status-im.transport.message.contact :as transport.contact]
            [status-im.transport.message.group-chat :as transport.group-chat]
            [status-im.transport.message.pairing :as transport.pairing]
            [status-im.transport.message.protocol :as protocol]))

(extend-type transport.group-chat/GroupMembershipUpdate
  protocol/StatusMessage
  (receive [this _ signature _ {:keys [dedup-id js-obj] :as cofx}]
    (group-chats/handle-membership-update-received cofx this signature {:dedup-id dedup-id
                                                                        :raw-payload (.-payload js-obj)})))

(extend-type transport.contact/ContactRequest
  protocol/StatusMessage
  (receive [this _ signature timestamp {:keys [js-obj] :as cofx}]
    (contact/receive-contact-request signature timestamp this (.-payload js-obj) cofx)))

(extend-type transport.contact/ContactRequestConfirmed
  protocol/StatusMessage
  (receive [this _ signature timestamp cofx]
    (contact/receive-contact-request-confirmation signature timestamp this cofx)))

(extend-type transport.contact/ContactUpdate
  protocol/StatusMessage
  (receive [this _ signature timestamp cofx]
    (contact/receive-contact-update signature timestamp this cofx)))

(extend-type transport.pairing/SyncInstallation
  protocol/StatusMessage
  (receive [this _ signature _ cofx]
    (pairing/handle-sync-installation cofx this signature)))

(extend-type transport.pairing/PairInstallation
  protocol/StatusMessage
  (receive [this _ signature timestamp cofx]
    (pairing/handle-pair-installation cofx this timestamp signature)))
