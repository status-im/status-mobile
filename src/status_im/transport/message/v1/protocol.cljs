(ns ^{:doc "Protocol API and protocol utils"}
 status-im.transport.message.v1.protocol
  (:require [status-im.utils.config :as config]
            [status-im.constants :as constants]
            [status-im.chat.core :as chat]
            [status-im.transport.db :as transport.db]
            [status-im.transport.message.core :as message]
            [status-im.transport.utils :as transport.utils]))

(def ^:private whisper-opts
  {:ttl       10 ;; ttl of 10 sec
   :powTarget config/pow-target
   :powTime   config/pow-time})

(defn init-chat
  "Initialises chat on protocol layer.
  2 arities are provided, 2arg arity initialises the chat with topic derived from first argument (`chat-id`),
  using the 3arg arity, you can specify the topic as well (second argument)"
  ([chat-id cofx]
   (init-chat chat-id (transport.utils/get-topic chat-id) cofx))
  ([chat-id topic {:keys [db] :as cofx}]
   {:db (assoc-in db [:transport/chats chat-id] (transport.db/create-chat topic))}))

#_(defn requires-ack [message-id chat-id {:keys [db] :as cofx}]
    {:db (update-in db [:transport/chats chat-id :pending-ack] conj message-id)})

#_(defn ack [message-id chat-id {:keys [db] :as cofx}]
    {:db (update-in db [:transport/chats chat-id :ack] conj message-id)})

(defn send
  "Sends the payload using symetric key and topic from db (looked up by `chat-id`)"
  [{:keys [payload chat-id success-event]} {:keys [db] :as cofx}]
  ;; we assume that the chat contains the contact public-key
  (let [{:keys [current-public-key web3]} db
        {:keys [sym-key-id topic]} (get-in db [:transport/chats chat-id])]
    {:shh/post [{:web3          web3
                 :success-event success-event
                 :message       (merge {:sig      current-public-key
                                        :symKeyID sym-key-id
                                        :payload  payload
                                        :topic    topic}
                                       whisper-opts)}]}))

(defn send-with-pubkey
  "Sends the payload using asymetric key (`:current-public-key` in db) and fixed discovery topic"
  [{:keys [payload chat-id success-event]} {:keys [db] :as cofx}]
  (let [{:keys [current-public-key web3]} db]
    {:shh/post [{:web3          web3
                 :success-event success-event
                 :message       (merge {:sig     current-public-key
                                        :pubKey  chat-id
                                        :payload payload
                                        :topic   (transport.utils/get-topic constants/contact-discovery)}
                                       whisper-opts)}]}))

(defn- prepare-recipients [public-keys db]
  (map (fn [public-key]
         (select-keys (get-in db [:transport/chats public-key]) [:topic :sym-key-id]))
       public-keys))

(defn multi-send-by-pubkey
  "Sends payload to multiple participants selected by `:public-keys` key. "
  [{:keys [payload public-keys success-event]} {:keys [db] :as cofx}]
  (let [{:keys [current-public-key web3]} db
        recipients                        (prepare-recipients public-keys db)]
    {:shh/multi-post {:web3          web3
                      :success-event success-event
                      :recipients    recipients
                      :message       (merge {:sig     current-public-key
                                             :payload payload}
                                            whisper-opts)}}))

;; TODO currently not used
(defrecord Ack [message-ids]
  message/StatusMessage
  (send [this cofx chat-id])
  (receive [this db chat-id sig]))

(defrecord Seen [message-ids]
  message/StatusMessage
  (send [this cofx chat-id])
  (receive [this cofx chat-id sig]))

(defrecord Message [content content-type message-type clock-value timestamp]
  message/StatusMessage
  (send [this chat-id cofx]
    (let [params     {:chat-id       chat-id
                      :payload       this
                      :success-event [:transport/set-message-envelope-hash
                                      chat-id
                                      (transport.utils/message-id this)
                                      message-type]}
          group-chat (get-in cofx [:db :chats chat-id :group-chat])]
      (if (or group-chat
              config/use-sym-key)
        (send params cofx)
        (send-with-pubkey params cofx))))
  (receive [this chat-id signature cofx]
    {:chat-received-message/add-fx
     [(assoc (into {} this)
             :message-id (transport.utils/message-id this)
             :chat-id chat-id
             :from signature)]}))

(defrecord MessagesSeen [message-ids]
  message/StatusMessage
  (send [this chat-id cofx]
    (send {:chat-id chat-id
           :payload this}
          cofx))
  (receive [this chat-id signature cofx]
    (chat/receive-seen chat-id signature this cofx)))
