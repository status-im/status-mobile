(ns ^{:doc "Protocol API and protocol utils"}
 status-im.transport.message.protocol
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(fx/defn send-chat-message [_ {:keys [chat-id
                                      text
                                      response-to
                                      ens-name
                                      message-type
                                      sticker
                                      content-type]
                               :as message}]
  {::json-rpc/call [{:method (if config/waku-enabled?
                               "wakuext_sendChatMessage"
                               "shhext_sendChatMessage")
                     :params [{:chatId chat-id
                               :text text
                               :responseTo response-to
                               :ensName ens-name
                               :sticker sticker
                               :contentType content-type}]
                     :on-success
                     #(re-frame/dispatch [:transport/message-sent % 1])
                     :on-failure #(log/error "failed to send a message" %)}]})
