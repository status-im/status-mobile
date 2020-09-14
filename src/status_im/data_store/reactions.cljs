(ns status-im.data-store.reactions
  (:require [clojure.set :as clojure.set]
            [status-im.ethereum.json-rpc :as json-rpc]))

(defn ->rpc [message]
  (-> message
      (clojure.set/rename-keys {:message-id        :messageId
                                :emoji-id          :emojiId
                                :chat-id           :localChatId
                                :message-type      :messageType
                                :emoji-reaction-id :id})))

(defn <-rpc [message]
  (-> message
      (dissoc :chat_id)
      (clojure.set/rename-keys {:messageId   :message-id
                                :localChatId :chat-id
                                :emojiId     :emoji-id
                                :messageType :message-type
                                :id          :emoji-reaction-id})))

(defn reactions-by-chat-id-rpc [chat-id
                                cursor
                                limit
                                on-success
                                on-failure]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method "emojiReactionsByChatID")
                     :params     [chat-id cursor limit]
                     :on-success (fn [result]
                                   (on-success (map <-rpc result)))
                     :on-failure on-failure}]})
