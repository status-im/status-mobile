(ns status-im.transport.impl.send
  (:require [status-im.group-chats.core :as group-chats]
            [status-im.utils.fx :as fx]
            [status-im.pairing.core :as pairing]
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
                        (select-keys
                         (get-in cofx [:db :contacts/contacts])
                         [chat-id])
                        nil
                        nil)]
      (fx/merge cofx
                (protocol/init-chat {:chat-id chat-id
                                     :resend? "contact-request"})
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
                         (transport.db/create-chat {:resend? "contact-request-confirmation"}))]
      (fx/merge cofx
                {:db            (assoc-in db
                                          [:transport/chats chat-id] updated-chat)
                 :data-store/tx [(transport-store/save-transport-tx {:chat-id chat-id
                                                                     :chat    updated-chat})]}
                (protocol/send-with-pubkey {:chat-id chat-id
                                            :payload this
                                            :success-event success-event})
                (pairing/send-installation-message-fx sync-message)))))

(fx/defn send-contact-update
  [{:keys [db] :as cofx} chat-id payload]
  (when-let [chat (get-in cofx [:db :transport/chats chat-id])]
    (let [updated-chat  (assoc chat :resend? "contact-update")
          tx            [(transport-store/save-transport-tx {:chat-id chat-id
                                                             :chat    updated-chat})]
          success-event [:transport/contact-message-sent chat-id]]
      (fx/merge cofx
                {:db (assoc-in db
                               [:transport/chats chat-id :resend?]
                               "contact-update")
                 :data-store/tx tx}
                (protocol/send-with-pubkey {:chat-id       chat-id
                                            :payload       payload
                                            :success-event success-event})))))
(extend-type transport.contact/ContactUpdate
  protocol/StatusMessage
  (send [this _ {:keys [db] :as cofx}]
    (let [contact-public-keys (reduce (fn [acc [_ {:keys [public-key dapp? pending?]}]]
                                        (if (and (not dapp?)
                                                 (not pending?))
                                          (conj acc public-key)
                                          acc))
                                      #{}
                                      (:contacts/contacts db))
          ;;NOTE: chats with contacts use public-key as chat-id
          send-contact-update-fxs (map #(send-contact-update % this) contact-public-keys)
          sync-message            (pairing/sync-installation-account-message cofx)
          fxs                     (conj send-contact-update-fxs
                                        (pairing/send-installation-message-fx sync-message))]
      (apply fx/merge cofx fxs))))
