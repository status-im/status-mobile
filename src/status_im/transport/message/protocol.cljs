(ns ^{:doc "Protocol API and protocol utils"}
 status-im.transport.message.protocol
  (:require [cljs.spec.alpha :as spec]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.transport.db :as transport.db]
            [status-im.utils.pairing :as pairing.utils]
            [status-im.transport.utils :as transport.utils]
            [status-im.tribute-to-talk.whitelist :as whitelist]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn discovery-topic-hash [] (transport.utils/get-topic constants/contact-discovery))

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

(defn send-public-message
  "Sends the payload to topic"
  [cofx chat-id success-event payload]
  {:shh/send-public-message [{:success-event success-event
                              :src     (multiaccounts.model/current-public-key cofx)
                              :chat    chat-id
                              :payload payload}]})

(fx/defn send-direct-message
  "Sends the payload using to dst"
  [cofx dst success-event payload]
  {:shh/send-direct-message [{:success-event  success-event
                              :src            (multiaccounts.model/current-public-key cofx)
                              :dst            dst
                              :payload        payload}]})

(fx/defn send-with-pubkey
  "Sends the payload using asymetric key (multiaccount `:public-key` in db) and fixed discovery topic"
  [cofx {:keys [payload chat-id success-event]}]
  (send-direct-message cofx
                       chat-id
                       success-event
                       payload))

(defrecord Message [content content-type message-type clock-value timestamp]
  StatusMessage
  (send [this chat-id {:keys [message] :as cofx}]
    (let [current-public-key (multiaccounts.model/current-public-key cofx)
          params             {:chat-id       chat-id
                              :payload       this
                              :success-event [:transport/message-sent
                                              chat-id
                                              message
                                              message-type]}]
      (case message-type
        :public-group-user-message
        (send-public-message cofx chat-id (:success-event params) this)
        :user-message
        (fx/merge cofx
                  (when (pairing.utils/has-paired-installations? cofx)
                    (send-direct-message current-public-key nil this))
                  (send-with-pubkey params)))))
  (receive [this chat-id signature timestamp cofx]
    (let [received-message-fx {:chat-received-message/add-fx
                               [(assoc (into {} this)
                                       :message-id
                                       (get-in cofx [:metadata :messageId])
                                       :chat-id chat-id
                                       :whisper-timestamp timestamp
                                       :alias (get-in cofx [:metadata :author :alias])
                                       :identicon (get-in cofx [:metadata :author :identicon])
                                       :from signature
                                       :metadata (:metadata cofx)
                                       :js-obj (:js-obj cofx))]}]
      (whitelist/filter-message cofx
                                received-message-fx
                                message-type
                                (get-in this [:content :tribute-transaction])
                                signature)))
  (validate [this]
    this))
