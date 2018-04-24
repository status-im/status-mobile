(ns ^{:doc "Contact request and update API"}
    status-im.transport.message.v1.contact
  (:require [re-frame.core :as re-frame]
            [status-im.transport.message.core :as message]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.transport.utils :as transport.utils]
            [status-im.ui.screens.contacts.core :as contacts]
            [status-im.utils.handlers-macro :as handlers-macro]))

(defrecord NewContactKey [sym-key topic message]
  message/StatusMessage
  (send [this chat-id cofx]
    (protocol/send-with-pubkey {:chat-id chat-id
                                :payload this}
                               cofx))
  (receive [this chat-id signature cofx]
    (let [on-success (fn [sym-key sym-key-id]
                       (re-frame/dispatch [:contact/add-new-sym-key
                                           {:sym-key-id sym-key-id
                                            :sym-key    sym-key
                                            :chat-id    chat-id
                                            :topic      topic
                                            :message    message}]))]
      (handlers-macro/merge-fx cofx
                         {:shh/add-new-sym-key {:web3       (get-in cofx [:db :web3])
                                                :sym-key    sym-key
                                                :on-success on-success}}
                         (protocol/init-chat chat-id topic)))))

(defrecord ContactRequest [name profile-image address fcm-token]
  message/StatusMessage
  (send [this chat-id {:keys [db random-id] :as cofx}]
    (let [message-id (transport.utils/message-id this)
          topic      (transport.utils/get-topic random-id)
          on-success (fn [sym-key sym-key-id]
                       (re-frame/dispatch [:contact/send-new-sym-key
                                           {:sym-key-id sym-key-id
                                            :sym-key    sym-key
                                            :chat-id    chat-id
                                            :topic      topic
                                            :message    this}]))]
      (handlers-macro/merge-fx cofx
                         {:shh/get-new-sym-key {:web3       (:web3 db)
                                                :on-success on-success}}
                         (protocol/init-chat chat-id topic)
                         #_(protocol/requires-ack message-id chat-id))))
  (receive [this chat-id signature {:keys [db] :as cofx}]
    (let [message-id (transport.utils/message-id this)]
      (when (protocol/is-new? message-id)
        (handlers-macro/merge-fx cofx
                           #_(protocol/ack message-id chat-id)
                           (contacts/receive-contact-request signature
                                                             this))))))

(defrecord ContactRequestConfirmed [name profile-image address fcm-token]
  message/StatusMessage
  (send [this chat-id cofx]
    (let [message-id (transport.utils/message-id this)]
      (handlers-macro/merge-fx cofx
                         #_(protocol/requires-ack message-id chat-id)
                         (protocol/send {:chat-id chat-id
                                         :payload this}))))
  (receive [this chat-id signature cofx]
    (let [message-id (transport.utils/message-id this)]
      (when (protocol/is-new? message-id)
        (handlers-macro/merge-fx cofx
                           #_(protocol/ack message-id chat-id)
                           (contacts/receive-contact-request-confirmation signature
                                                                          this))))))

(defrecord ContactUpdate [name profile-image]
  message/StatusMessage
  (send [this _ {:keys [db] :as cofx}]
    (let [message-id (transport.utils/message-id this)
          public-keys (remove nil? (map :public-key (vals (:contacts/contacts db))))]
      (handlers-macro/merge-fx cofx
                         (protocol/multi-send-with-pubkey {:public-keys public-keys
                                                           :payload     this}))))
  (receive [this chat-id signature cofx]
    (let [message-id (transport.utils/message-id this)]
      (when (protocol/is-new? message-id)
        (handlers-macro/merge-fx cofx
                           (contacts/receive-contact-update chat-id
                                                            signature
                                                            this))))))
