(ns status-im.transport.impl.send
  (:require [status-im.group-chats.core :as group-chats]
            [status-im.utils.fx :as fx]
            [status-im.pairing.core :as pairing]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.data-store.transport :as transport-store]
            [status-im.transport.db :as transport.db]
            [status-im.transport.message.pairing :as transport.pairing]
            [status-im.transport.message.contact :as transport.contact]
            [status-im.transport.message.group-chat :as transport.group-chat]
            [status-im.transport.message.protocol :as protocol]))

(extend-type transport.group-chat/GroupMembershipUpdate
  protocol/StatusMessage
  (send [this chat-id cofx]
    (group-chats/send-membership-update cofx this chat-id)))

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
                (protocol/init-chat {:chat-id    chat-id
                                     :one-to-one true
                                     :resend?    "contact-request"})
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
          success-event [:transport/contact-message-sent chat-id]
          chat         (get-in db [:transport/chats chat-id])
          updated-chat (if chat
                         (assoc chat :resend? "contact-request-confirmation")
                         (transport.db/create-chat {:resend?    "contact-request-confirmation"
                                                    :one-to-one true}))]
      (fx/merge cofx
                {:db            (assoc-in db
                                          [:transport/chats chat-id] updated-chat)
                 :data-store/tx [(transport-store/save-transport-tx {:chat-id chat-id
                                                                     :chat    updated-chat})]}
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
