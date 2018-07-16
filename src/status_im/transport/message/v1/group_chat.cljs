(ns ^{:doc "Group chat API"}
 status-im.transport.message.v1.group-chat
  (:require [re-frame.core :as re-frame]
            [status-im.thread :as status-im.thread]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.transport.message.core :as message]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.transport.utils :as transport.utils]))

;; NOTE: We ignore the chat-id from the send and receive method.
;; The chat-id is usually deduced from the filter the message comes from but not in that case because it is sent
;; individually to each participant of the group.
;; In order to be able to determine which group the message belongs to the chat-id is therefore
;; passed in the message itself
(defrecord NewGroupKey [chat-id sym-key message]
  message/StatusMessage
  (send [this _ cofx]
    (let [public-keys (get-in cofx [:db :chats chat-id :contacts])]
      (protocol/multi-send-by-pubkey {:public-keys public-keys
                                      :chat-id     chat-id
                                      :payload     this}
                                     cofx)))
  (receive [this _ signature timestamp {:keys [db] :as cofx}]
    (handlers-macro/merge-fx
     cofx
     {:shh/add-new-sym-keys
      [{:web3       (:web3 db)
        :sym-key    sym-key
        :on-success (fn [sym-key sym-key-id]
                      (status-im.thread/dispatch
                       [:group/add-new-sym-key
                        {:chat-id    chat-id
                         :signature  signature
                         :timestamp  timestamp
                         :sym-key    sym-key
                         :sym-key-id sym-key-id
                         :message    message}]))}]}
     (protocol/init-chat {:chat-id chat-id}))))

(defn- user-is-group-admin? [chat-id cofx]
  (= (get-in cofx [:db :chats chat-id :group-admin])
     (get-in cofx [:db :current-public-key])))

(defn send-new-group-key [message chat-id cofx]
  (when (user-is-group-admin? chat-id cofx)
    {:shh/get-new-sym-keys [{:web3       (get-in cofx [:db :web3])
                             :on-success (fn [sym-key sym-key-id]
                                           (status-im.thread/dispatch
                                            [:group/send-new-sym-key
                                             {:chat-id    chat-id
                                              :sym-key    sym-key
                                              :sym-key-id sym-key-id
                                              :message    message}]))}]}))

(defn- init-chat-if-new [chat-id cofx]
  (if (nil? (get-in cofx [:db :transport/chats chat-id]))
    (protocol/init-chat {:chat-id chat-id} cofx)))

(defrecord GroupAdminUpdate [chat-name participants]
  message/StatusMessage
  (send [this chat-id cofx]
    (handlers-macro/merge-fx cofx
                             (init-chat-if-new chat-id)
                             (send-new-group-key this chat-id))))

(defrecord GroupLeave []
  message/StatusMessage
  (send [this chat-id cofx]
    (protocol/send {:chat-id       chat-id
                    :payload       this
                    :success-event [:group/unsubscribe-from-chat chat-id]}
                   cofx)))
