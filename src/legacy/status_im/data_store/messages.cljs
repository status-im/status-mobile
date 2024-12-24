(ns legacy.status-im.data-store.messages
  (:require
    [clojure.set :as set]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(defn ->rpc
  [{:keys [content] :as message}]
  (cond-> message
    content
    (assoc :text    (:text content)
           :sticker (:sticker content))
    :always
    (set/rename-keys {:chat-id           :chat_id
                      :whisper-timestamp :whisperTimestamp
                      :community-id      :communityId
                      :clock-value       :clock})))

(defn <-status-link-previews-rpc
  [preview]
  (-> preview
      (update :community
              set/rename-keys
              {:communityId        :community-id
               :displayName        :display-name
               :membersCount       :members-count
               :activeMembersCount :active-members-count})
      (update-in [:community :banner] set/rename-keys {:dataUri :data-uri})
      (update-in [:community :icon] set/rename-keys {:dataUri :data-uri})))

(defn ->status-link-previews-rpc
  [preview]
  (-> preview
      (update :community
              set/rename-keys
              {:community-id         :communityId
               :display-name         :displayName
               :members-count        :membersCount
               :active-members-count :activeMembersCount})
      (update-in [:community :banner] set/rename-keys {:data-uri :dataUri})
      (update-in [:community :icon] set/rename-keys {:data-uri :dataUri})))

(defn <-link-preview-rpc
  [preview]
  (-> preview
      (update :thumbnail set/rename-keys {:dataUri :data-uri})
      (update :favicon set/rename-keys {:dataUri :data-uri})))

(defn ->link-preview-rpc
  [preview]
  (-> preview
      (update :thumbnail set/rename-keys {:data-uri :dataUri})
      (update :favicon set/rename-keys {:data-uri :dataUri})))

(defn <-rpc
  [message]
  (-> message
      (set/rename-keys
       {:id                       :message-id
        :whisperTimestamp         :whisper-timestamp
        :compressedKey            :compressed-key
        :editedAt                 :edited-at
        :contactVerificationState :contact-verification-state
        :contactRequestState      :contact-request-state
        :commandParameters        :command-parameters
        :gapParameters            :gap-parameters
        :messageType              :message-type
        :localChatId              :chat-id
        :communityId              :community-id
        :contentType              :content-type
        :clock                    :clock-value
        :quotedMessage            :quoted-message
        :outgoingStatus           :outgoing-status
        :audioDurationMs          :audio-duration-ms
        :deleted                  :deleted?
        :deletedForMe             :deleted-for-me?
        :deletedBy                :deleted-by
        :albumId                  :album-id
        :imageWidth               :image-width
        :imageHeight              :image-height
        :new                      :new?
        :albumImagesCount         :album-images-count
        :displayName              :display-name
        :linkPreviews             :link-previews
        :statusLinkPreviews       :status-link-previews
        :bridgeMessage            :bridge-message})
      (update :bridge-message
              set/rename-keys
              {:bridgeName :bridge-name
               :userName   :user-name
               :userAvatar :user-avatar})
      (update :link-previews #(map <-link-preview-rpc %))
      (update :status-link-previews #(map <-status-link-previews-rpc %))
      (update :quoted-message
              set/rename-keys
              {:parsedText       :parsed-text
               :deleted          :deleted?
               :deletedForMe     :deleted-for-me?
               :communityId      :community-id
               :albumImagesCount :album-images-count
               :bridgeMessage    :bridge-message})
      (update :outgoing-status keyword)
      (update :command-parameters
              set/rename-keys
              {:transactionHash :transaction-hash
               :commandState    :command-state})
      (assoc :content  {:chat-id     (:chatId message)
                        :text        (:text message)
                        :image       (:image message)
                        :sticker     (:sticker message)
                        :ens-name    (:ensName message)
                        :line-count  (:lineCount message)
                        :parsed-text (:parsedText message)
                        :links       (:links message)
                        :rtl?        (:rtl message)
                        :response-to (:responseTo message)}
             :outgoing (boolean (:outgoingStatus message)))
      (dissoc :ensName :chatId :text :rtl :responseTo :image :sticker :lineCount :parsedText :links)))

(defn messages-by-chat-id-rpc
  [chat-id
   cursor
   limit
   on-success
   on-error]
  {:json-rpc/call [{:method     "wakuext_chatMessages"
                    :params     [chat-id cursor limit]
                    :on-success (fn [result]
                                  (on-success (update result :messages #(map <-rpc %))))
                    :on-error   on-error}]})

(defn mark-seen-rpc
  [chat-id ids on-success]
  {:json-rpc/call [{:method     "wakuext_markMessagesSeen"
                    :params     [chat-id ids]
                    :on-success #(do
                                   (log/debug "successfully marked as seen" %)
                                   (when on-success (on-success chat-id ids %)))
                    :on-error   #(log/error "failed to get messages" %)}]})

(defn delete-message-rpc
  [id]
  {:json-rpc/call [{:method     "wakuext_deleteMessage"
                    :params     [id]
                    :on-success #(log/debug "successfully deleted message" id)
                    :on-error   #(log/error "failed to delete message" % id)}]})

(defn delete-messages-from-rpc
  [author]
  {:json-rpc/call [{:method     "wakuext_deleteMessagesFrom"
                    :params     [author]
                    :on-success #(log/debug "successfully deleted messages from" author)
                    :on-error   #(log/error "failed to delete messages from" % author)}]})

(defn delete-messages-by-chat-id-rpc
  [chat-id]
  {:json-rpc/call [{:method     "wakuext_deleteMessagesByChatID"
                    :params     [chat-id]
                    :on-success #(log/debug "successfully deleted messages by chat-id" chat-id)
                    :on-error   #(log/error "failed to delete messages by chat-id" % chat-id)}]})

(rf/defn delete-message
  [_cofx id]
  (delete-message-rpc id))

(rf/defn delete-messages-from
  [_cofx author]
  (delete-messages-from-rpc author))

(rf/defn mark-messages-seen
  [_cofx chat-id ids on-success]
  (mark-seen-rpc chat-id ids on-success))

(rf/defn delete-messages-by-chat-id
  [_cofx chat-id]
  (delete-messages-by-chat-id-rpc chat-id))
