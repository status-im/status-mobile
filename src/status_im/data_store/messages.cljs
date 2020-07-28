(ns status-im.data-store.messages
  (:require [clojure.set :as clojure.set]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [status-im.waku.core :as waku]
            [taoensso.timbre :as log]))

(defn ->rpc [{:keys [content] :as message}]
  (cond-> message
    content
    (assoc :text (:text content)
           :sticker (:sticker content))
    :always
    (clojure.set/rename-keys {:chat-id :chatId
                              :clock-value :clock})))

(defn <-rpc [message]
  (-> message
      (clojure.set/rename-keys {:id :message-id
                                :whisperTimestamp :whisper-timestamp
                                :commandParameters :command-parameters
                                :messageType :message-type
                                :localChatId :chat-id
                                :contentType  :content-type
                                :clock  :clock-value
                                :quotedMessage :quoted-message
                                :outgoingStatus :outgoing-status
                                :audioDurationMs :audio-duration-ms})

      (update :outgoing-status keyword)
      (update :command-parameters clojure.set/rename-keys {:transactionHash :transaction-hash
                                                           :commandState :command-state})
      (assoc :content {:chat-id (:chatId message)
                       :text (:text message)
                       :image (:image message)
                       :sticker (:sticker message)
                       :ens-name (:ensName message)
                       :line-count (:lineCount message)
                       :parsed-text (:parsedText message)
                       :rtl? (:rtl message)
                       :response-to (:responseTo message)}
             :outgoing (boolean (:outgoingStatus message)))
      (dissoc :ensName :chatId :text :rtl :responseTo :image :sticker :lineCount :parsedText)))

(defn update-outgoing-status-rpc [waku-enabled? message-id status]
  {::json-rpc/call [{:method (json-rpc/call-ext-method waku-enabled? "updateMessageOutgoingStatus")
                     :params [message-id status]
                     :on-success #(log/debug "updated message outgoing stauts" message-id status)
                     :on-failure #(log/error "failed to update message outgoing status" message-id status %)}]})

(defn messages-by-chat-id-rpc [waku-enabled?
                               chat-id
                               cursor
                               limit
                               on-success
                               on-failure]
  {::json-rpc/call [{:method (json-rpc/call-ext-method waku-enabled? "chatMessages")
                     :params [chat-id cursor limit]
                     :on-success (fn [result]
                                   (on-success (update result :messages #(map <-rpc %))))
                     :on-failure on-failure}]})

(defn mark-seen-rpc [waku-enabled? chat-id ids on-success]
  {::json-rpc/call [{:method (json-rpc/call-ext-method waku-enabled? "markMessagesSeen")
                     :params [chat-id ids]
                     :on-success #(do
                                    (log/debug "successfully marked as seen" %)
                                    (when on-success (on-success chat-id ids %)))
                     :on-failure #(log/error "failed to get messages" %)}]})

(defn delete-message-rpc [waku-enabled? id]
  {::json-rpc/call [{:method (json-rpc/call-ext-method waku-enabled? "deleteMessage")
                     :params [id]
                     :on-success #(log/debug "successfully deleted message" id)
                     :on-failure #(log/error "failed to delete message" % id)}]})

(defn delete-messages-from-rpc [waku-enabled? author]
  {::json-rpc/call [{:method (json-rpc/call-ext-method waku-enabled? "deleteMessagesFrom")
                     :params [author]
                     :on-success #(log/debug "successfully deleted messages from" author)
                     :on-failure #(log/error "failed to delete messages from" % author)}]})

(defn delete-messages-by-chat-id-rpc [waku-enabled? chat-id]
  {::json-rpc/call [{:method (json-rpc/call-ext-method waku-enabled? "deleteMessagesByChatID")
                     :params [chat-id]
                     :on-success #(log/debug "successfully deleted messages by chat-id" chat-id)
                     :on-failure #(log/error "failed to delete messages by chat-id" % chat-id)}]})

(fx/defn delete-message [cofx id]
  (delete-message-rpc (waku/enabled? cofx) id))

(fx/defn delete-messages-from [cofx author]
  (delete-messages-from-rpc (waku/enabled? cofx) author))

(fx/defn mark-messages-seen [cofx chat-id ids on-success]
  (mark-seen-rpc (waku/enabled? cofx) chat-id ids on-success))

(fx/defn update-outgoing-status [cofx message-id status]
  (update-outgoing-status-rpc (waku/enabled? cofx) message-id status))

(fx/defn delete-messages-by-chat-id [cofx chat-id]
  (delete-messages-by-chat-id-rpc (waku/enabled? cofx) chat-id))
