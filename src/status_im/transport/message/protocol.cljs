(ns ^{:doc "Protocol API and protocol utils"}
 status-im.transport.message.protocol
  (:require [cljs.spec.alpha :as spec]
            [status-im.accounts.db :as accounts.db]
            [status-im.chat.core :as chat]
            [status-im.transport.db :as transport.db]
            [status-im.transport.utils :as transport.utils]
            [status-im.transport.partitioned-topic :as transport.topic]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defprotocol StatusMessage
  "Protocol for the messages that are sent through the transport layer"
  (send [this chat-id cofx] "Method producing all effects necessary for sending the message record")
  (receive [this chat-id signature timestamp cofx] "Method producing all effects necessary for receiving the message record")
  (validate [this] "Method returning the message if it is valid or nil if it is not"))

(def whisper-opts
  {;; time drift that is tolerated by whisper, in seconds
   :whisper-drift-tolerance 10
   ;; ttl of 10 sec
   :ttl                     10
   :powTarget               config/pow-target
   :powTime                 config/pow-time})

(fx/defn init-chat
  "Initialises chat on protocol layer.
  If topic is not passed as argument it is derived from `chat-id`"
  [{:keys [db now]}
   {:keys [chat-id topic one-to-one resend?]}]
  {:db (assoc-in db
                 [:transport/chats chat-id]
                 (transport.db/create-chat {:topic      topic
                                            :one-to-one one-to-one
                                            :resend?    resend?
                                            :now        now}))})

(defn send-public-message
  "Sends the payload to topic"
  [{:keys [db] :as cofx} chat-id success-event payload]
  (let [{:keys [web3]} db]
    {:shh/send-public-message [{:web3 web3
                                :success-event success-event
                                :src     (accounts.db/current-public-key cofx)
                                :chat    chat-id
                                :payload payload}]}))

(fx/defn send-with-sym-key
  "Sends the payload using symetric key and topic from db (looked up by `chat-id`)"
  [{:keys [db] :as cofx} {:keys [payload chat-id success-event]}]
  ;; we assume that the chat contains the contact public-key
  (let [{:keys [web3]} db
        {:keys [sym-key-id topic]} (get-in db [:transport/chats chat-id])]
    {:shh/post [{:web3          web3
                 :success-event success-event
                 :message       (merge {:sig      (accounts.db/current-public-key cofx)
                                        :symKeyID sym-key-id
                                        :payload  payload
                                        :topic    (or topic
                                                      (transport.topic/public-key->discovery-topic-hash chat-id))}
                                       whisper-opts)}]}))

(fx/defn send-direct-message
  "Sends the payload using to dst"
  [{:keys [db] :as cofx} dst success-event payload]
  (let [{:keys [web3]} db]
    {:shh/send-direct-message [{:web3 web3
                                :success-event success-event
                                :src     (accounts.db/current-public-key cofx)
                                :dst     dst
                                :payload payload}]}))

(fx/defn send-with-pubkey
  "Sends the payload using asymetric key (account `:public-key` in db) and fixed discovery topic"
  [{:keys [db] :as cofx} {:keys [payload chat-id success-event]}]
  (let [{:keys [web3]} db]
    (let [pfs? (get-in db [:account/account :settings :pfs?])]
      (if (and config/pfs-toggle-visible? pfs?)
        (send-direct-message cofx
                             chat-id
                             success-event
                             payload)
        {:shh/post [{:web3          web3
                     :success-event success-event
                     :message       (merge {:sig     (accounts.db/current-public-key cofx)
                                            :pubKey  chat-id
                                            :payload payload
                                            :topic   (transport.topic/public-key->discovery-topic-hash chat-id)}
                                           whisper-opts)}]}))))

(defrecord Message [content content-type message-type clock-value timestamp]
  StatusMessage
  (send [this chat-id {:keys [message-id] :as cofx}]
    (let [current-public-key (accounts.db/current-public-key cofx)
          params             {:chat-id       chat-id
                              :payload       this
                              :success-event [:transport/message-sent
                                              chat-id
                                              message-id
                                              message-type]}]
      (case message-type
        :public-group-user-message
        (send-with-sym-key cofx params)

        :user-message
        (fx/merge cofx
                  (send-direct-message current-public-key nil this)
                  (send-with-pubkey params)))))
  (receive [this chat-id signature _ cofx]
    {:chat-received-message/add-fx
     [(assoc (into {} this)
             :old-message-id (transport.utils/old-message-id this)
             :message-id (transport.utils/message-id
                          signature
                          (.-payload (:js-obj cofx)))
             :chat-id chat-id
             :raw-payload-hash (transport.utils/sha3
                                (.-payload (:js-obj cofx)))
             :from signature
             :dedup-id (:dedup-id cofx)
             :js-obj (:js-obj cofx))]})
  (validate [this]
    (if (spec/valid? :message/message this)
      this
      (log/warn "failed to validate Message" (spec/explain-str :message/message this)))))

(defrecord MessagesSeen [message-ids]
  StatusMessage
  (send [this chat-id cofx]
    (send-with-pubkey cofx {:chat-id chat-id
                            :payload this}))
  (receive [this chat-id signature _ cofx]
    (chat/receive-seen cofx chat-id signature this))
  (validate [this]
    (when (spec/valid? :message/message-seen this)
      this)))
