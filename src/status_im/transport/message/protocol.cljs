(ns ^{:doc "Protocol API and protocol utils"}
 status-im.transport.message.protocol
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(fx/defn send-chat-message [cofx {:keys [chat-id
                                         text
                                         response-to
                                         ens-name
                                         image-path
                                         audio-path
                                         audio-duration-ms
                                         message-type
                                         sticker
                                         content-type]
                                  :as message}]
  {::json-rpc/call [{:method (json-rpc/call-ext-method
                              (get-in cofx [:db :multiaccount :waku-enabled])
                              "sendChatMessage")
                     :params [{:chatId chat-id
                               :text text
                               :responseTo response-to
                               :ensName ens-name
                               :imagePath image-path
                               :audioPath audio-path
                               :audioDurationMs audio-duration-ms
                               :sticker sticker
                               :contentType content-type}]
                     :on-success
                     #(re-frame/dispatch [:transport/message-sent % 1])
                     :on-failure #(log/error "failed to send a message" %)}]})
