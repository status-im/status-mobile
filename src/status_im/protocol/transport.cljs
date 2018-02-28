(ns status-im.protocol.transport
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.protocol.web3.utils :as web3.utils]
            [status-im.protocol.chat :as chat]))

(defn message-id [message]
  (web3.utils/sha3 (pr-str message)))

(defn get-topic [chat-id]
  (subs (web3.utils/sha3 chat-id) 0 10))

(defprotocol StatusMessage
  "Protocol for transport layed status messages"
  (send [this chat-id cofx])
  (receive [this chat-id signature cofx]))

(defrecord ContactRequest [name profile-image address fcm-token]
  message/StatusMessage
  (send [this chat-id cofx]
    (let [message-id (message-id this)]
      (chat/send! {:web3 web3
                   :message (-> (into {} this)
                                (assoc :to chat-id))})))
  (receive [this chat-id signature {:keys [db] :as cofx}]))

(defrecord ContactRequestConfirmed [name profile-image address fcm-token]
  message/StatusMessage
  (send [this chat-id cofx]
    (let [message-id (message-id this)]
      (chat/send! {:web3 web3
                   :message (-> (into {} this)
                                (assoc :to chat-id))})))
  (receive [this chat-id signature cofx]))

(defrecord ContactMessage [content content-type message-type to-clock-value timestamp]
  message/StatusMessage
  (send [this chat-id cofx]
    (chat/send! {:web3 web3
                 :message (-> (into {} this)
                              (assoc :to chat-id))}))
  (receive [this chat-id signature cofx]))
