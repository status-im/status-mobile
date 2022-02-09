(ns ^{:doc "Protocol API and protocol utils"}
 status-im.transport.message.protocol
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn build-message [{:keys [chat-id
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

(fx/defn share-image-to-contacts-pressed
  {:events [::share-image-to-contacts-pressed]}
  [cofx user-pk contacts message-id content-type]
  (let [pks (if (seq user-pk)
              (conj contacts user-pk)
              contacts)]
    (when (seq pks)
      {::json-rpc/call [{:method     "wakuext_shareImageMessage"
                         :params     [{:users pks
                                       :id message-id
                                       :contentType content-type}]
                         :js-response true
                         :on-success #(re-frame/dispatch [:transport/message-sent %])
                         :on-error   #(do
                                        (log/error "failed to share image message" %)
                                        (re-frame/dispatch [::failed-to-share-image %]))}]})))   

(fx/defn send-chat-messages [_ messages]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method "sendChatMessages")
                     :params     [(mapv build-message messages)]
                     :js-response true
                     :on-success #(re-frame/dispatch [:transport/message-sent %])
                     :on-failure #(log/error "failed to send a message" %)}]})

(fx/defn send-reaction [_ {:keys [message-id chat-id emoji-id]}]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method
                                  "sendEmojiReaction")
                     :params     [chat-id message-id emoji-id]
                     :js-response true
                     :on-success #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                     :on-failure #(log/error "failed to send a reaction" %)}]})

(fx/defn send-retract-reaction [_ {:keys [emoji-reaction-id] :as reaction}]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method
                                  "sendEmojiReactionRetraction")
                     :params     [emoji-reaction-id]
                     :js-response true
                     :on-success #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                     :on-failure #(log/error "failed to send a reaction retraction" %)}]})
