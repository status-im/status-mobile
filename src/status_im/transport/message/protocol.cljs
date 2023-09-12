(ns ^{:doc "Protocol API and protocol utils"} status-im.transport.message.protocol
  (:require [clojure.set :as set]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(defn- link-preview->rpc
  [preview]
  (update preview
          :thumbnail
          (fn [thumbnail]
            (set/rename-keys thumbnail {:data-uri :dataUri}))))

(defn build-message
  [msg]
  (-> msg
      (update :link-previews #(map link-preview->rpc %))
      (set/rename-keys
       {:album-id          :albumId
        :audio-duration-ms :audioDurationMs
        :audio-path        :audioPath
        :chat-id           :chatId
        :community-id      :communityId
        :content-type      :contentType
        :ens-name          :ensName
        :image-height      :imageHeight
        :image-path        :imagePath
        :image-width       :imageWidth
        :link-previews     :linkPreviews
        :response-to       :responseTo
        :sticker           :sticker
        :text              :text})))

(rf/defn send-chat-messages
  [_ messages]
  {:json-rpc/call [{:method      "wakuext_sendChatMessages"
                    :params      [(mapv build-message messages)]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:transport/message-sent %])
                    :on-error    #(do
                                    (log/warn "failed to send a message" %)
                                    (js/alert (str "failed to send a message: " %)))}]})

(rf/defn send-reaction
  [_ {:keys [message-id chat-id emoji-id]}]
  {:json-rpc/call [{:method      "wakuext_sendEmojiReaction"
                    :params      [chat-id message-id emoji-id]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to send a reaction" %)}]})

(rf/defn send-retract-reaction
  [_ {:keys [emoji-reaction-id]}]
  {:json-rpc/call [{:method      "wakuext_sendEmojiReactionRetraction"
                    :params      [emoji-reaction-id]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to send a reaction retraction" %)}]})
