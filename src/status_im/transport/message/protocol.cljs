(ns ^{:doc "Protocol API and protocol utils"} status-im.transport.message.protocol
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn build-message
  [{:keys [chat-id
           text
           response-to
           ens-name
           community-id
           image-path
           audio-path
           audio-duration-ms
           sticker
           content-type]}]
  {:chatId          chat-id
   :text            text
   :responseTo      response-to
   :ensName         ens-name
   :imagePath       image-path
   :audioPath       audio-path
   :audioDurationMs audio-duration-ms
   :communityId     community-id
   :sticker         sticker
   :contentType     content-type})

(fx/defn send-chat-messages
  [_ messages]
  {:json-rpc/call [{:method      "wakuext_sendChatMessages"
                    :params      [(mapv build-message messages)]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:transport/message-sent %])
                    :on-error    #(do
                                    (log/warn "failed to send a message" %)
                                    (js/alert (str "failed to send a message: " %)))}]})

(fx/defn send-reaction
  [_ {:keys [message-id chat-id emoji-id]}]
  {:json-rpc/call [{:method      "wakuext_sendEmojiReaction"
                    :params      [chat-id message-id emoji-id]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to send a reaction" %)}]})

(fx/defn send-retract-reaction
  [_ {:keys [emoji-reaction-id]}]
  {:json-rpc/call [{:method      "wakuext_sendEmojiReactionRetraction"
                    :params      [emoji-reaction-id]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to send a reaction retraction" %)}]})
