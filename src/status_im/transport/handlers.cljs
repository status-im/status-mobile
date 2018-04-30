(ns ^{:doc "Events for message handling"}
    status-im.transport.handlers
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.transport.message.core :as message]
            [status-im.transport.core :as transport]
            [status-im.chat.models :as models.chat]
            [status-im.utils.datetime :as datetime]
            [taoensso.timbre :as log]
            [status-im.transport.utils :as transport.utils]
            [cljs.reader :as reader]
            [status-im.transport.message.transit :as transit]
            [status-im.transport.shh :as shh]
            [status-im.transport.filters :as filters]
            [status-im.transport.message.core :as message]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.transport.message.v1.contact :as v1.contact]
            [status-im.transport.message.v1.group-chat :as v1.group-chat]
            [status-im.data-store.transport :as transport-store]))

(handlers/register-handler-fx
  :protocol/receive-whisper-message
  [re-frame/trim-v (re-frame/inject-cofx :random-id)]
  (fn [cofx [js-error js-message chat-id]]
    (let [{:keys [payload sig]} (js->clj js-message :keywordize-keys true)
          status-message        (-> payload
                                    transport.utils/to-utf8
                                    transit/deserialize)]
      (when (and sig status-message)
        (message/receive status-message (or chat-id sig) sig cofx)))))

(handlers/register-handler-fx
  :protocol/send-status-message-success
  [re-frame/trim-v]
  (fn [{:keys [db] :as cofx} [_ resp]]
    (log/debug :send-status-message-success resp)))

(handlers/register-handler-fx
  :protocol/send-status-message-error
  [re-frame/trim-v]
  (fn [{:keys [db] :as cofx} [err]]
    (log/error :send-status-message-error err)))

(handlers/register-handler-fx
 :contact/send-new-sym-key
 (fn [{:keys [db random-id] :as cofx} [_ {:keys [chat-id topic message sym-key sym-key-id]}]]
   (let [{:keys [web3 current-public-key]} db
         chat-transport-info               (-> (get-in db [:transport/chats chat-id])
                                               (assoc :sym-key-id sym-key-id)
                                               ;;TODO (yenda) remove once go implements persistence
                                               (assoc :sym-key sym-key))]
     (handlers-macro/merge-fx cofx
                              {:db (assoc-in db [:transport/chats chat-id :sym-key-id] sym-key-id)
                               :shh/add-filter {:web3       web3
                                                :sym-key-id sym-key-id
                                                :topic      topic
                                                :chat-id    chat-id}
                               :data-store/tx  [(transport-store/save-transport-tx {:chat-id chat-id
                                                                                    :chat    chat-transport-info})]}
                              (message/send (v1.contact/NewContactKey. sym-key topic message)
                                            chat-id)))))

(handlers/register-handler-fx
 :contact/add-new-sym-key
 (fn [{:keys [db] :as cofx} [_ {:keys [sym-key-id sym-key chat-id topic message]}]]
   (let [{:keys [web3 current-public-key]} db
         chat-transport-info               (-> (get-in db [:transport/chats chat-id])
                                               (assoc :sym-key-id sym-key-id)
                                               ;;TODO (yenda) remove once go implements persistence
                                               (assoc :sym-key sym-key))]
     (handlers-macro/merge-fx cofx
                              {:db (assoc-in db [:transport/chats chat-id :sym-key-id] sym-key-id)
                               :shh/add-filter {:web3       web3
                                                :sym-key-id sym-key-id
                                                :topic      topic
                                                :chat-id    chat-id}
                               :data-store/tx  [(transport-store/save-transport-tx {:chat-id chat-id
                                                                                    :chat    chat-transport-info})]}
                              (message/receive message chat-id chat-id)))))

#_(handlers/register-handler-fx
   :send-test-message
   (fn [cofx [this timer chat-id n]]
     (if (zero? n)
       (println  "Time: " (str (- (inst-ms (js/Date.)) @timer)))
       (handlers-macro/merge-fx cofx
                                {:dispatch [this timer chat-id (dec n)]}
                                (message/send (protocol/map->Message {:content      (str n)
                                                                      :content-type "text/plain"
                                                                      :message-type :user-message
                                                                      :clock-value  n
                                                                      :timestamp    (str (inst-ms (js/Date.)))})
                                              chat-id)))))


(handlers/register-handler-fx
 :group/unsubscribe-from-chat
 [re-frame/trim-v]
 (fn [cofx [chat-id]]
   (transport/unsubscribe-from-chat chat-id cofx)))

(handlers/register-handler-fx
 :group/send-new-sym-key
 [re-frame/trim-v]
 ;; this is the event that is called when we want to send a message that required first
 ;; some async operations
 (fn [{:keys [db] :as cofx} [{:keys [chat-id message sym-key sym-key-id]}]]
   (let [{:keys [web3]} db]
     (handlers-macro/merge-fx cofx
                              {:db             (assoc-in db [:transport/chats chat-id :sym-key-id] sym-key-id)
                               :shh/add-filter {:web3       web3
                                                :sym-key-id sym-key-id
                                                :topic      (transport.utils/get-topic chat-id)
                                                :chat-id    chat-id}
                               :data-store/tx  [(transport-store/save-transport-tx {:chat-id chat-id
                                                                                    :chat (-> (get-in db [:transport/chats chat-id])
                                                                                              (assoc :sym-key-id sym-key-id)
                                                                                              ;;TODO (yenda) remove once go implements persistence
                                                                                              (assoc :sym-key sym-key))})]}
                              (message/send (v1.group-chat/NewGroupKey. chat-id sym-key message) chat-id)))))

(handlers/register-handler-fx
 :group/add-new-sym-key
 [re-frame/trim-v (re-frame/inject-cofx :random-id)]
 (fn [{:keys [db] :as cofx} [{:keys [sym-key-id sym-key chat-id signature message]}]]
   (let [{:keys [web3 current-public-key]} db
         fx {:db (assoc-in db [:transport/chats chat-id :sym-key-id] sym-key-id)
             :shh/add-filter {:web3       web3
                              :sym-key-id sym-key-id
                              :topic      (transport.utils/get-topic chat-id)
                              :chat-id    chat-id}
             :data-store/tx  [(transport-store/save-transport-tx {:chat-id chat-id
                                                                  :chat (-> (get-in db [:transport/chats chat-id])
                                                                            (assoc :sym-key-id sym-key-id)
                                                                            ;;TODO (yenda) remove once go implements persistence
                                                                            (assoc :sym-key sym-key))})]}]
     ;; if new sym-key is wrapping some message, call receive on it as well, if not just update the transport layer
     (if message
       (handlers-macro/merge-fx cofx fx (message/receive message chat-id signature))
       fx))))
