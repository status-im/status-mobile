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

(def ^:private legacy-ref->new-path
  {["transactor" :command 83 "send"]    ["send" #{:personal-chats}]
   ["transactor" :command 83 "request"] ["request" #{:personal-chats}]})

(defn- legacy->new-command-content [{:keys [command-path command-ref] :as content}]
  (if command-path
    ;; `:command-path` set, message produced by newer app version, nothing to do
    content
    ;; we have to look up `:command-path` based on legacy `:command-ref` value (`release/0.9.25` and older) and assoc it to content
    (assoc content :command-path (get legacy-ref->new-path command-ref))))

(defn- legacy->new-message-data [content content-type]
  ;; handling only the text content case
  (cond
    (= content-type constants/content-type-text)
    (if (and (map? content) (string? (:text content)))
      ;; correctly formatted map
      [content content-type]
      ;; create safe `{:text string-content}` value from anything else
      [{:text (str content)} content-type])
    (or (= content-type constants/content-type-command)
        (= content-type constants/content-type-command-request))
    [(legacy->new-command-content content) constants/content-type-command]
    :else
    [content content-type]))

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
    (let [[new-content new-content-type] (legacy->new-message-data content
                                                                   content-type)
          new-message (assoc this
                             :content new-content
                             :content-type new-content-type)]
      {:chat-received-message/add-fx
       [(assoc (into {} new-message)
               :message-id (transport.utils/message-id this)
               ;; TODO(rasom): remove this condition
               ;; on when 0.9.29 will not be available for users
               :message-id-old-format (transport.utils/message-id-old-format this)
               :show? true
               :chat-id chat-id
               :from signature
               :js-obj (:js-obj cofx))]}))
  (validate [this]
    (if (spec/valid? :message/message new-message)
      new-message
      (log/warn "failed to validate Message" (spec/explain :message/message new-message)))))

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
