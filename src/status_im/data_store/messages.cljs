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

(defn prepare-content [content]
  (if (string? content)
    content
    (utils.types/clj->json content)))

(defn ->rpc [message]
  (-> message
      (dissoc :js-obj :dedup-id)
      (update :message-type name)
      (update :outgoing-status #(if % (name %) ""))
      (utils/update-if-present :content prepare-content)
      (clojure.set/rename-keys {:message-id :id
                                :whisper-timestamp :whisperTimestamp
                                :message-type :messageType
                                :chat-id :chatId
                                :content-type :contentType
                                :clock-value :clockValue
                                :outgoing-status :outgoingStatus})
      (assoc :replyTo (get-in message [:content :response-to-v2]))))

(defn update-quoted-message [message]
  (let [parsed-content (utils/safe-read-message-content (get-in message [:quotedMessage :content]))]
    (cond-> message
      parsed-content
      (assoc :quoted-message {:from (get-in message [:quotedMessage :from])
                              :text (:text parsed-content)})
      :always
      (dissoc message :quotedMessage))))

(defn <-rpc [message]
  (when-let [parsed-content (utils/safe-read-message-content (:content message))]
    (let [outgoing-status (when-not (empty? (:outgoingStatus message))
                            (keyword (:outgoingStatus message)))]

      (-> message
          (update :messageType keyword)
          (update :outgoingStatus keyword)
          (assoc :content parsed-content
                 :outgoingStatus outgoing-status
                 :outgoing outgoing-status)
          (update-quoted-message)
          (clojure.set/rename-keys {:id :message-id
                                    :whisperTimestamp :whisper-timestamp
                                    :messageType :message-type
                                    :chatId :chat-id
                                    :contentType  :content-type
                                    :replyTo :reply-to
                                    :clockValue  :clock-value
                                    :outgoingStatus :outgoing-status})))))

(defn update-outgoing-status-rpc [message-id status]
  {::json-rpc/call [{:method "shhext_updateMessageOutgoingStatus"
                     :params [message-id status]
                     :on-success #(log/debug "updated message outgoing stauts" message-id status)
                     :on-failure #(log/error "failed to update message outgoing status" message-id status %)}]})

(defn save-messages-rpc [messages]
  (let [confirmations (keep :metadata messages)]
    (json-rpc/call {:method "shhext_saveMessages"
                    :params [(map ->rpc messages)]
                    :on-success #(re-frame/dispatch [:message/messages-persisted confirmations])
                    :on-failure #(log/error "failed to save messages" %)})))

(defn messages-by-chat-id-rpc [chat-id cursor limit on-success]
  {::json-rpc/call [{:method "shhext_chatMessages"
                     :params [chat-id cursor limit]
                     :on-success (fn [result]
                                   (on-success (update result :messages #(map <-rpc %))))
                     :on-failure #(log/error "failed to get messages" %)}]})

(defn mark-seen-rpc [ids]
  {::json-rpc/call [{:method "shhext_markMessagesSeen"
                     :params [ids]
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
 ::save-message
 (fn [messages]
   (save-messages-rpc messages)))

(fx/defn save-message [cofx message]
  {::save-message [message]})

(fx/defn delete-message [cofx id]
  (delete-message-rpc id))

(fx/defn delete-messages-from [cofx author]
  (delete-messages-from-rpc author))

(fx/defn mark-messages-seen [_ ids]
  (mark-seen-rpc ids))

(fx/defn update-outgoing-status [cofx message-id status]
  (update-outgoing-status-rpc message-id status))

(fx/defn delete-messages-by-chat-id [cofx chat-id]
  (delete-messages-by-chat-id-rpc chat-id))
