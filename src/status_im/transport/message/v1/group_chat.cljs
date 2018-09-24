(ns ^{:doc "Group chat API"}
 status-im.transport.message.v1.group-chat
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.transport.message.core :as message]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.transport.utils :as transport.utils]))

(defn- user-is-group-admin? [chat-id cofx]
  (= (get-in cofx [:db :chats chat-id :group-admin])
     (get-in cofx [:db :current-public-key])))

(defn- init-chat-if-new [chat-id cofx]
  (if (nil? (get-in cofx [:db :transport/chats chat-id]))
    (protocol/init-chat {:chat-id chat-id} cofx)))

(defrecord GroupAdminUpdate [chat-name participants chat-id]
  message/StatusMessage
  (send [this chat-id cofx]
    (let [{:keys [current-public-key web3]} (:db cofx)] (handlers-macro/merge-fx
                                                         cofx
                                                         {:shh/send-group-message {:web3 web3
                                                                                   :src     current-public-key
                                                                                   :dsts    (disj participants current-public-key)
                                                                                   :payload this}}
                                                         (init-chat-if-new chat-id)))))

(defrecord GroupChatCreate [chat-name participants chat-id]
  message/StatusMessage
  (send [this chat-id cofx]
    (let [{:keys [current-public-key web3]} (:db cofx)]
      (handlers-macro/merge-fx
       cofx
       {:shh/send-group-message {:web3 web3
                                 :src     current-public-key
                                 :dsts    (disj participants current-public-key)
                                 :payload this}}
       (init-chat-if-new chat-id)))))

(defrecord GroupLeave []
  message/StatusMessage
  (send [this chat-id cofx]
    (protocol/send {:chat-id       chat-id
                    :payload       this
                    :success-event [:group/unsubscribe-from-chat chat-id]}
                   cofx)))
