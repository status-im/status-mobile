(ns ^{:doc "Contact request and update API"}
 status-im.transport.message.v1.contact
  (:require [re-frame.core :as re-frame]
            [status-im.transport.message.core :as message]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.handlers :as handlers]
            [status-im.data-store.transport :as transport-store]))

(defrecord NewContactKey [sym-key topic message]
  message/StatusMessage
  (send [this chat-id cofx]
    (let [success-event [:transport/set-contact-message-envelope-hash chat-id]]
      (protocol/send-with-pubkey {:chat-id       chat-id
                                  :payload       this
                                  :success-event success-event}
                                 cofx)))
  (receive [this chat-id signature cofx]
    (let [on-success (fn [sym-key sym-key-id]
                       (re-frame/dispatch [:contact/add-new-sym-key
                                           {:sym-key-id sym-key-id
                                            :sym-key    sym-key
                                            :chat-id    chat-id
                                            :topic      topic
                                            :message    message}]))]
      (handlers-macro/merge-fx cofx
                               {:shh/add-new-sym-keys [{:web3       (get-in cofx [:db :web3])
                                                        :sym-key    sym-key
                                                        :on-success on-success}]}
                               (protocol/init-chat {:chat-id chat-id
                                                    :topic   topic})))))

(defrecord ContactRequest [name profile-image address fcm-token]
  message/StatusMessage
  (send [this chat-id {:keys [db random-id] :as cofx}]
    (let [topic      (transport.utils/get-topic random-id)
          on-success (fn [sym-key sym-key-id]
                       (re-frame/dispatch [:contact/send-new-sym-key
                                           {:sym-key-id sym-key-id
                                            :sym-key    sym-key
                                            :chat-id    chat-id
                                            :topic      topic
                                            :message    this}]))]
      (handlers-macro/merge-fx cofx
                               {:shh/get-new-sym-keys [{:web3       (:web3 db)
                                                        :on-success on-success}]}
                               (protocol/init-chat {:chat-id chat-id
                                                    :topic   topic
                                                    :resend? "contact-request"})))))

(defrecord ContactRequestConfirmed [name profile-image address fcm-token]
  message/StatusMessage
  (send [this chat-id {:keys [db] :as cofx}]
    (let [success-event [:transport/set-contact-message-envelope-hash chat-id]
          chat         (get-in db [:transport/chats chat-id])
          updated-chat (assoc chat :resend? "contact-request-confirmation")]
      (handlers-macro/merge-fx cofx
                               {:db            (assoc-in db
                                                         [:transport/chats chat-id :resend?]
                                                         "contact-request-confirmation")
                                :data-store/tx [(transport-store/save-transport-tx {:chat-id chat-id
                                                                                    :chat    updated-chat})]}
                               (protocol/send {:chat-id chat-id
                                               :payload this
                                               :success-event success-event})))))

(defrecord ContactUpdate [name profile-image]
  message/StatusMessage
  (send [this _ {:keys [db] :as cofx}]
    (let [public-keys (into #{} (remove nil? (map :public-key (vals (:contacts/contacts db)))))
          recipients  (filter #(public-keys (first %)) (:transport/chats db))]
      (handlers-macro/merge-effects
       cofx
       (fn [[chat-id chat] temp-cofx]
         (let [updated-chat  (assoc chat :resend? "contact-update")
               tx            [(transport-store/save-transport-tx {:chat-id chat-id
                                                                  :chat    updated-chat})]
               success-event [:transport/set-contact-message-envelope-hash chat-id]]
           (handlers-macro/merge-fx temp-cofx
                                    {:db            (assoc-in db
                                                              [:transport/chats chat-id :resend?]
                                                              "contact-update")
                                     :data-store/tx tx}
                                    (protocol/send {:chat-id       chat-id
                                                    :payload       this
                                                    :success-event success-event}))))
       recipients))))
