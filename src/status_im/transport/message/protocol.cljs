(ns ^{:doc "Protocol API and protocol utils"}
 status-im.transport.message.protocol
  (:require [cljs.spec.alpha :as spec]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [re-frame.core :as re-frame]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.transport.db :as transport.db]
            [status-im.utils.pairing :as pairing.utils]
            [status-im.transport.utils :as transport.utils]
            [status-im.tribute-to-talk.whitelist :as whitelist]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defprotocol StatusMessage
  "Protocol for the messages that are sent through the transport layer"
  (send [this chat-id cofx] "Method producing all effects necessary for sending the message record")
  (receive [this chat-id signature timestamp cofx] "Method producing all effects necessary for receiving the message record")
  (validate [this] "Method returning the message if it is valid or nil if it is not"))

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

(fx/defn send-chat-message [_ {:keys [chat-id
                                      text
                                      response-to
                                      ens-name
                                      message-type
                                      sticker
                                      content-type]
                               :as message}]
  {::json-rpc/call [{:method "shhext_sendChatMessage"
                     :params [{:chatId chat-id
                               :text text
                               :responseTo response-to
                               :ensName ens-name
                               :sticker sticker
                               :contentType content-type}]
                     :on-success
                     #(re-frame/dispatch [:transport/message-sent % 1])
                     :on-failure #(log/error "failed to send a message" %)}]})

(defrecord Message [content content-type message-type clock-value timestamp]
  StatusMessage
  (send [this chat-id {:keys [message] :as cofx}])
  (validate [this]
    (if (spec/valid? :message/message this)
      this
      (log/warn "failed to validate Message" (spec/explain-str :message/message this)))))
