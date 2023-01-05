(ns status-im.data-store.reactions
  (:require [clojure.set :as set]))

(defn ->rpc
  [message]
  (-> message
      (set/rename-keys {:message-id        :messageId
                        :emoji-id          :emojiId
                        :chat-id           :localChatId
                        :message-type      :messageType
                        :emoji-reaction-id :id})))

(defn <-rpc
  [message]
  (-> message
      (dissoc :chat_id)
      (set/rename-keys {:messageId   :message-id
                        :localChatId :chat-id
                        :emojiId     :emoji-id
                        :messageType :message-type
                        :id          :emoji-reaction-id})))

(defn reactions-by-chat-id-rpc
  [chat-id
   cursor
   limit
   on-success
   on-error]
  {:json-rpc/call [{:method     "wakuext_emojiReactionsByChatID"
                    :params     [chat-id cursor limit]
                    :on-success (fn [result]
                                  (on-success (map <-rpc result)))
                    :on-error   on-error}]})
