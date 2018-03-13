(ns status-im.transport.message.v1.contact
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.transport.message.core :as message]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.transport.utils :as transport.utils]))

(defrecord NewContactKey [sym-key message]
  message/StatusMessage
  (send [this chat-id cofx]
    (protocol/send-with-pubkey {:chat-id chat-id
                                :payload this}
                               cofx))
  (receive [this chat-id signature cofx]
    (handlers/merge-fx cofx
                       {:shh/add-new-sym-key {:web3 (get-in cofx [:db :web3])
                                              :sym-key sym-key
                                              :chat-id chat-id
                                              :message message
                                              :success-event ::add-new-sym-key}}
                       (protocol/init-chat chat-id))))

(defrecord ContactRequest [name profile-image address fcm-token]
  message/StatusMessage
  (send [this chat-id cofx]
    (let [message-id (transport.utils/message-id this)]
      (handlers/merge-fx cofx
                         {:shh/get-new-sym-key {:web3 (get-in cofx [:db :web3])
                                                :chat-id chat-id
                                                :message this
                                                :success-event ::send-new-sym-key}}
                         (protocol/init-chat chat-id)
                         (protocol/requires-ack message-id chat-id))))
  (receive [this chat-id signature {:keys [db] :as cofx}]
    (let [message-id (transport.utils/message-id this)]
      (when (protocol/is-new? message-id)
        (handlers/merge-fx cofx
                           (protocol/ack message-id chat-id)
                           (message/receive-contact-request signature
                                                            this))))))

(defrecord ContactRequestConfirmed [name profile-image address fcm-token]
  message/StatusMessage
  (send [this chat-id cofx]
    (let [message-id (transport.utils/message-id this)]
      (handlers/merge-fx cofx
                         (protocol/requires-ack message-id chat-id)
                         (protocol/send {:web3    (get-in cofx [:db :web3])
                                         :chat-id chat-id
                                         :payload this}))))
  (receive [this chat-id signature cofx]
    (let [message-id (transport.utils/message-id this)]
      (when (protocol/is-new? message-id)
        (handlers/merge-fx cofx
                           (protocol/ack message-id chat-id)
                           (message/receive-contact-request-confirmation signature
                                                                         this))))))

(defrecord ContactMessage [content content-type message-type to-clock-value timestamp]
  message/StatusMessage
  (send [this chat-id cofx]
    (protocol/send {:chat-id       chat-id
                    :payload       this
                    :success-event [:update-message-status
                                    chat-id
                                    (transport.utils/message-id this)
                                    (get-in cofx [:db :current-public-key])
                                    :sent]}
                   cofx))
  (receive [this chat-id signature cofx]
    {:dispatch [:chat-received-message/add (assoc (into {} this)
                                                  :message-id (transport.utils/message-id this)
                                                  :chat-id    chat-id
                                                  :from       signature)]}))

(handlers/register-handler-fx
  ::send-new-sym-key
  (fn [{:keys [db] :as cofx} [_ {:keys [chat-id message sym-key sym-key-id]}]]
    (let [{:keys [web3 current-public-key]} db]
      (handlers/merge-fx cofx
                         {:db (assoc-in db [:transport/chats chat-id :sym-key-id] sym-key-id)
                          :shh/add-filter {:web3 web3
                                           :sym-key-id sym-key-id
                                           :topic (transport.utils/get-topic current-public-key)
                                           :chat-id chat-id}
                          :data-store.transport/save {:chat-id chat-id
                                                      :chat (-> (get-in db [:transport/chats chat-id])
                                                                (assoc :sym-key-id sym-key-id)
                                                                ;;TODO (yenda) remove once go implements persistence
                                                                (assoc :sym-key sym-key))}}
                         (message/send (NewContactKey. sym-key message)
                                       chat-id)))))

(handlers/register-handler-fx
  ::add-new-sym-key
  (fn [{:keys [db] :as cofx} [_ {:keys [sym-key-id sym-key chat-id message]}]]
    (let [{:keys [web3 current-public-key]} db]
      (handlers/merge-fx cofx
                         {:db (assoc-in db [:transport/chats chat-id :sym-key-id] sym-key-id)
                          :shh/add-filter {:web3 web3
                                           :sym-key-id sym-key-id
                                           :topic (transport.utils/get-topic current-public-key)
                                           :chat-id chat-id}
                          :data-store.transport/save {:chat-id chat-id
                                                      :chat (-> (get-in db [:transport/chats chat-id])
                                                                (assoc :sym-key-id sym-key-id)
                                                                ;;TODO (yenda) remove once go implements persistence
                                                                (assoc :sym-key sym-key))}}
                         (message/receive message chat-id chat-id)))))

(handlers/register-handler-fx
  :send-test-message
  (fn [cofx [this timer chat-id n]]
    (if (zero? n)
      (println  "Time: " (str (- (inst-ms (js/Date.)) @timer)))
      (handlers/merge-fx cofx
                         {:dispatch [this timer chat-id (dec n)]}
                         (message/send (map->ContactMessage {:content (str n)
                                                             :content-type "text/plain"
                                                             :message-type :user-message
                                                             :clock-value n
                                                             :timestamp (str (inst-ms (js/Date.)))})
                                       chat-id)))))

#_(re-frame/dispatch [:send-test-message (atom (inst-ms (js/Date.))) "0x04ae4c56aa22e668fd4659acadf8a141981cbae4b8d8c2d63edbeecc16fca1338c43ae7f2bde4b34f877f41149f97c32f1b4189c3f7f87f1e4d1fa6d833bc0272e" 100])
