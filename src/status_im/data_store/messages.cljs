(ns status-im.data-store.messages
  (:require [clojure.set :as clojure.set]
            [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [clojure.string :as string]
            [taoensso.timbre :as log]
            [re-frame.core :as re-frame]
            [status-im.utils.types :as utils.types]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.constants :as constants]
            [status-im.utils.core :as utils]))

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
                                :messageType :message-type
                                :localChatId :chat-id
                                :contentType  :content-type
                                :clock  :clock-value
                                :quotedMessage :quoted-message
                                :outgoingStatus :outgoing-status})

      (update :outgoing-status keyword)
      (assoc :content {:chat-id (:chatId message)
                       :text (:text message)
                       :sticker (:sticker message)
                       :ens-name (:ensName message)
                       :line-count (:lineCount message)
                       :parsed-text (:parsedText message)
                       :rtl (:rtl message)
                       :response-to (:responseTo message)}
             :outgoing (boolean (:outgoingStatus message)))
      (dissoc :ensName :chatId :text :rtl :responseTo :sticker :lineCount :parsedText)))

(defn update-outgoing-status-rpc [message-id status]
  {::json-rpc/call [{:method "shhext_updateMessageOutgoingStatus"
                     :params [message-id status]
                     :on-success #(log/debug "updated message outgoing stauts" message-id status)
                     :on-failure #(log/error "failed to update message outgoing status" message-id status %)}]})

(defn save-system-messages-rpc [messages]
  (json-rpc/call {:method "shhext_addSystemMessages"
                  :params [(map ->rpc messages)]
                  :on-success #(re-frame/dispatch [:messages/system-messages-saved (map <-rpc %)])
                  :on-failure #(log/error "failed to save messages" %)}))

(defn messages-by-chat-id-rpc [chat-id cursor limit on-success]
  {::json-rpc/call [{:method "shhext_chatMessages"
                     :params [chat-id cursor limit]
                     :on-success (fn [result]
                                   (on-success (update result :messages #(map <-rpc %))))
                     :on-failure #(log/error "failed to get messages" %)}]})

(defn mark-seen-rpc [chat-id ids]
  {::json-rpc/call [{:method "shhext_markMessagesSeen"
                     :params [chat-id ids]
                     :on-success #(log/debug "successfully marked as seen")
                     :on-failure #(log/error "failed to get messages" %)}]})

(defn delete-message-rpc [id]
  {::json-rpc/call [{:method "shhext_deleteMessage"
                     :params [id]
                     :on-success #(log/debug "successfully deleted message" id)
                     :on-failure #(log/error "failed to delete message" % id)}]})

(defn delete-messages-from-rpc [author]
  {::json-rpc/call [{:method "shhext_deleteMessagesFrom"
                     :params [author]
                     :on-success #(log/debug "successfully deleted messages from" author)
                     :on-failure #(log/error "failed to delete messages from" % author)}]})

(defn delete-messages-by-chat-id-rpc [chat-id]
  {::json-rpc/call [{:method "shhext_deleteMessagesByChatID"
                     :params [chat-id]
                     :on-success #(log/debug "successfully deleted messages by chat-id" chat-id)
                     :on-failure #(log/error "failed to delete messages by chat-id" % chat-id)}]})

(re-frame/reg-fx
 ::save-system-message
 (fn [messages]
   (save-system-messages-rpc messages)))

(fx/defn save-system-messages [cofx messages]
  {::save-system-message messages})

(fx/defn delete-message [cofx id]
  (delete-message-rpc id))

(fx/defn delete-messages-from [cofx author]
  (delete-messages-from-rpc author))

(fx/defn mark-messages-seen [_ chat-id ids]
  (mark-seen-rpc chat-id ids))

(fx/defn update-outgoing-status [cofx message-id status]
  (update-outgoing-status-rpc message-id status))

(fx/defn delete-messages-by-chat-id [cofx chat-id]
  (delete-messages-by-chat-id-rpc chat-id))
