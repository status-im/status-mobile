(ns status-im.transport.impl.send
  (:require [status-im.group-chats.core :as group-chats]
            [status-im.utils.fx :as fx]
            [status-im.pairing.core :as pairing]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.transport.db :as transport.db]
            [status-im.transport.message.pairing :as transport.pairing]
            [status-im.transport.message.contact :as transport.contact]
            [status-im.transport.message.protocol :as protocol]))

(extend-type transport.pairing/PairInstallation
  protocol/StatusMessage
  (send [this _ cofx]
    (pairing/send-pair-installation cofx this)))

(extend-type transport.pairing/SyncInstallation
  protocol/StatusMessage
  (send [this _ cofx]
    (pairing/send-sync-installation cofx this)))

(extend-type transport.contact/ContactRequest
  protocol/StatusMessage
  (send [this chat-id cofx]
    (let [sync-message (transport.pairing/SyncInstallation.
                        {chat-id (pairing/contact->pairing
                                  (get-in cofx [:db :contacts/contacts chat-id]))}
                        nil
                        nil)]
      (fx/merge cofx
                (protocol/send-with-pubkey {:chat-id chat-id
                                            :payload this
                                            :success-event [:transport/contact-message-sent chat-id]})
                (pairing/send-installation-message-fx sync-message)))))

(extend-type transport.contact/ContactRequestConfirmed
  protocol/StatusMessage
  (send [this chat-id {:keys [db] :as cofx}]
    (let [sync-message (transport.pairing/SyncInstallation.
                        (select-keys
                         (get-in cofx [:db :contacts/contacts])
                         [chat-id])
                        nil
                        nil)
          success-event [:transport/contact-message-sent chat-id]]
      (fx/merge cofx
                (protocol/send-with-pubkey {:chat-id chat-id
                                            :payload this
                                            :success-event success-event})
                (pairing/send-installation-message-fx sync-message)))))

(extend-type transport.contact/ContactUpdate
  protocol/StatusMessage
  (send [this _ {:keys [db] :as cofx}]
    (let [send-contact-update-fxs (multiaccounts.update/send-contact-update cofx this)
          sync-message            (pairing/sync-installation-multiaccount-message cofx)
          fxs                     (conj send-contact-update-fxs
                                        (pairing/send-installation-message-fx sync-message))]
      (apply fx/merge cofx fxs))))
