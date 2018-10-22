(ns ^{:doc "Protocol API and protocol utils"}
 status-im.transport.message.protocol
  (:require [cljs.spec.alpha :as spec]
            [status-im.chat.core :as chat]
            [status-im.constants :as constants]
            [status-im.transport.db :as transport.db]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defprotocol StatusMessage
  "Protocol for the messages that are sent through the transport layer"
  (send [this chat-id cofx] "Method producing all effects necessary for sending the message record")
  (receive [this chat-id signature timestamp cofx] "Method producing all effects necessary for receiving the message record")
  (validate [this] "Method returning the message if it is valid or nil if it is not"))

(def ^:private whisper-opts
  {:ttl       10 ;; ttl of 10 sec
   :powTarget config/pow-target
   :powTime   config/pow-time})

(fx/defn init-chat
  "Initialises chat on protocol layer.
  If topic is not passed as argument it is derived from `chat-id`"
  [{:keys [db now]}
   {:keys [chat-id topic resend?]}]
  {:db (assoc-in db
                 [:transport/chats chat-id]
                 (transport.db/create-chat {:topic   topic
                                            :resend? resend?
                                            :now     now}))})

(fx/defn send-with-sym-key
  "Sends the payload using symetric key and topic from db (looked up by `chat-id`)"
  [{:keys [db] :as cofx} {:keys [payload chat-id success-event]}]
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

(fx/defn send-direct-message
  "Sends the payload using to dst"
  [{:keys [db] :as cofx} dst success-event payload]
  (let [{:keys [current-public-key web3]} db]
    {:shh/send-direct-message [{:web3 web3
                                :success-event success-event
                                :src     current-public-key
                                :dst     dst
                                :payload payload}]}))

(defn send-public-message
  "Sends the payload to topic"
  [{:keys [db] :as cofx} chat-id success-event payload]
  (let [{:keys [current-public-key web3]} db]
    {:shh/send-public-message [{:web3 web3
                                :success-event success-event
                                :src     current-public-key
                                :chat    chat-id
                                :payload payload}]}))

(fx/defn send-with-pubkey
  "Sends the payload using asymetric key (`:current-public-key` in db) and fixed discovery topic"
  [{:keys [db] :as cofx} {:keys [payload chat-id success-event]}]
  (let [{:keys [current-public-key web3]} db]
    {:shh/post [{:web3          web3
                 :success-event success-event
                 :message       (merge {:sig     current-public-key
                                        :pubKey  chat-id
                                        :payload payload
                                        :topic   (transport.utils/get-topic constants/contact-discovery)}
                                       whisper-opts)}]}))

(defrecord Message [content content-type message-type clock-value timestamp]
  StatusMessage
  (send [this chat-id cofx]
    (let [params     {:chat-id       chat-id
                      :payload       this
                      :success-event [:transport/message-sent
                                      chat-id
                                      (transport.utils/message-id this)
                                      message-type]}]
      (case message-type
        :public-group-user-message
        (if config/pfs-encryption-enabled?
          (send-public-message
           cofx
           chat-id
           (:success-event params)
           this)
          (send-with-sym-key cofx params))

        :user-message
        (if config/pfs-encryption-enabled?
          (send-direct-message
           cofx
           chat-id
           (:success-event params)
           this)
          (send-with-pubkey cofx params)))))
  (receive [this chat-id signature _ cofx]
    (let [old-content      (if (or (= content-type constants/content-type-command)
                                   (= content-type constants/content-type-command-request))
                             (dissoc content :command-path)
                             (:text content))
          old-content-type (if (= ["request" #{:personal-chats}] (:command-path content))
                             constants/content-type-command-request
                             content-type)
          old-message      (Message. old-content old-content-type message-type
                                     clock-value timestamp)]
      {:chat-received-message/add-fx
       [(assoc (into {} this)
               :message-id (transport.utils/message-id this)
               ;; TODO(rasom): remove this condition
               ;; on when 0.9.29 will not be available for users
               :message-id-old-format (transport.utils/message-id-old-format old-message)
               :show? true
               :chat-id chat-id
               :from signature
               :js-obj (:js-obj cofx))]}))
  (validate [this]
    (if (spec/valid? :message/message this)
      this
      (log/warn "failed to validate Message" (spec/explain :message/message this)))))

(defrecord MessagesSeen [message-ids]
  StatusMessage
  (send [this chat-id cofx]
    (if config/pfs-encryption-enabled?
      (send-direct-message cofx
                           chat-id
                           nil
                           this)
      (send-with-pubkey cofx {:chat-id chat-id
                              :payload this})))
  (receive [this chat-id signature _ cofx]
    (chat/receive-seen cofx chat-id signature this))
  (validate [this]
    (when (spec/valid? :message/message-seen this)
      this)))
