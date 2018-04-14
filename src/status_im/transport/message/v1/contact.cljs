(ns ^{:doc "Contact request and update API"}
    status-im.transport.message.v1.contact
  (:require [re-frame.core :as re-frame]
            [status-im.transport.message.core :as message]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.transport.utils :as transport.utils]
            [status-im.ui.screens.contacts.core :as contacts]
            [status-im.utils.handlers :as handlers]))

(defrecord NewContactKey [sym-key topic message]
  message/StatusMessage
  (send [this chat-id cofx]
    (protocol/send-with-pubkey {:chat-id chat-id
                                :payload this}
                               cofx))
  (receive [this chat-id signature cofx]
    (let [on-success (fn [sym-key sym-key-id]
                       (re-frame/dispatch [::add-new-sym-key {:sym-key-id sym-key-id
                                                              :sym-key    sym-key
                                                              :chat-id    chat-id
                                                              :topic      topic
                                                              :message    message}]))]
      (handlers/merge-fx cofx
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
                       (re-frame/dispatch [::send-new-sym-key {:sym-key-id sym-key-id
                                                               :sym-key    sym-key
                                                               :chat-id    chat-id
                                                               :topic      topic
                                                               :message    this}]))]
      (handlers/merge-fx cofx
                         {:shh/get-new-sym-key {:web3       (:web3 db)
                                                :on-success on-success}}
                         (protocol/init-chat chat-id topic)
                         #_(protocol/requires-ack message-id chat-id))))
  (receive [this chat-id signature {:keys [db] :as cofx}]
    (let [message-id (transport.utils/message-id this)]
      (when (protocol/is-new? message-id)
        (handlers/merge-fx cofx
                           #_(protocol/ack message-id chat-id)
                           (contacts/receive-contact-request signature
                                                             this))))))

(defrecord ContactRequestConfirmed [name profile-image address fcm-token]
  message/StatusMessage
  (send [this chat-id cofx]
    (let [message-id (transport.utils/message-id this)]
      (handlers/merge-fx cofx
                         #_(protocol/requires-ack message-id chat-id)
                         (protocol/send {:chat-id chat-id
                                         :payload this}))))
  (receive [this chat-id signature cofx]
    (let [message-id (transport.utils/message-id this)]
      (when (protocol/is-new? message-id)
        (handlers/merge-fx cofx
                           #_(protocol/ack message-id chat-id)
                           (contacts/receive-contact-request-confirmation signature
                                                                          this))))))

(defrecord ContactUpdate [name profile-image]
  message/StatusMessage
  (send [this _ {:keys [db] :as cofx}]
    (let [message-id (transport.utils/message-id this)
          public-keys (remove nil? (map :public-key (vals (:contacts/contacts db))))]
      (handlers/merge-fx cofx
                         (protocol/multi-send-with-pubkey {:public-keys public-keys
                                                           :payload     this}))))
  (receive [this chat-id signature cofx]
    (let [message-id (transport.utils/message-id this)]
      (when (protocol/is-new? message-id)
        (handlers/merge-fx cofx
                           (contacts/receive-contact-update chat-id
                                                            signature
                                                            this))))))

(handlers/register-handler-fx
  ::send-new-sym-key
  (fn [{:keys [db random-id] :as cofx} [_ {:keys [chat-id topic message sym-key sym-key-id]}]]
    (let [{:keys [web3 current-public-key]} db
          chat-transport-info               (-> (get-in db [:transport/chats chat-id])
                                                (assoc :sym-key-id sym-key-id)
                                                ;;TODO (yenda) remove once go implements persistence
                                                (assoc :sym-key sym-key))]
      (handlers/merge-fx cofx
                         {:db (assoc-in db [:transport/chats chat-id :sym-key-id] sym-key-id)
                          :shh/add-filter {:web3       web3
                                           :sym-key-id sym-key-id
                                           :topic      topic
                                           :chat-id    chat-id}
                          :data-store.transport/save {:chat-id chat-id
                                                      :chat    chat-transport-info}}
                         (message/send (NewContactKey. sym-key topic message)
                                       chat-id)))))

(handlers/register-handler-fx
  ::add-new-sym-key
  (fn [{:keys [db] :as cofx} [_ {:keys [sym-key-id sym-key chat-id topic message]}]]
    (let [{:keys [web3 current-public-key]} db
          chat-transport-info               (-> (get-in db [:transport/chats chat-id])
                                                (assoc :sym-key-id sym-key-id)
                                                ;;TODO (yenda) remove once go implements persistence
                                                (assoc :sym-key sym-key))]
      (handlers/merge-fx cofx
                         {:db (assoc-in db [:transport/chats chat-id :sym-key-id] sym-key-id)
                          :shh/add-filter {:web3       web3
                                           :sym-key-id sym-key-id
                                           :topic      topic
                                           :chat-id    chat-id}
                          :data-store.transport/save {:chat-id chat-id
                                                      :chat    chat-transport-info}}
                         (message/receive message chat-id chat-id)))))

#_(handlers/register-handler-fx
    :send-test-message
    (fn [cofx [this timer chat-id n]]
      (if (zero? n)
        (println  "Time: " (str (- (inst-ms (js/Date.)) @timer)))
        (handlers/merge-fx cofx
                           {:dispatch [this timer chat-id (dec n)]}
                           (message/send (protocol/map->Message {:content      (str n)
                                                                 :content-type "text/plain"
                                                                 :message-type :user-message
                                                                 :clock-value  n
                                                                 :timestamp    (str (inst-ms (js/Date.)))})
                                         chat-id)))))
