(ns ^{:doc "Contact request and update API"}
 status-im.transport.message.v1.contact
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.transport :as transport-store]
            [status-im.transport.message.core :as message]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.fx :as fx]
            [cljs.spec.alpha :as spec]))

(defrecord ContactRequest [name profile-image address fcm-token]
  message/StatusMessage
  (send [this chat-id {:keys [db random-id-generator] :as cofx}]
    (let [topic      (transport.utils/get-topic (random-id-generator))
          on-success (fn [sym-key sym-key-id]
                       (re-frame/dispatch [:contact/send-new-sym-key
                                           {:sym-key-id sym-key-id
                                            :sym-key    sym-key
                                            :chat-id    chat-id
                                            :topic      topic
                                            :message    this}]))]
      (fx/merge cofx
                {:shh/get-new-sym-keys [{:web3       (:web3 db)
                                         :on-success on-success}]}
                (protocol/init-chat {:chat-id chat-id
                                     :topic   topic
                                     :resend? "contact-request"}))))
  (validate [this]
    (when (spec/valid? :message/contact-request this)
      this)))

(defrecord ContactRequestConfirmed [name profile-image address fcm-token]
  message/StatusMessage
  (send [this chat-id {:keys [db] :as cofx}]
    (let [success-event [:transport/set-contact-message-envelope-hash chat-id]
          chat         (get-in db [:transport/chats chat-id])
          updated-chat (assoc chat :resend? "contact-request-confirmation")]
      (fx/merge cofx
                {:db            (assoc-in db
                                          [:transport/chats chat-id :resend?]
                                          "contact-request-confirmation")
                 :data-store/tx [(transport-store/save-transport-tx {:chat-id chat-id
                                                                     :chat    updated-chat})]}
                (protocol/send-with-pubkey {:chat-id chat-id
                                            :payload this
                                            :success-event success-event}))))
  (validate [this]
    (when (spec/valid? :message/contact-request-confirmed this)
      this)))

(fx/defn send-contact-update
  [{:keys [db] :as cofx} chat-id payload]
  (when-let [chat (get-in cofx [:db :transport/chats chat-id])]
    (let [updated-chat  (assoc chat :resend? "contact-update")
          tx            [(transport-store/save-transport-tx {:chat-id chat-id
                                                             :chat    updated-chat})]
          success-event [:transport/set-contact-message-envelope-hash chat-id]]
      (fx/merge cofx
                {:db (assoc-in db
                               [:transport/chats chat-id :resend?]
                               "contact-update")
                 :data-store/tx tx}
                (protocol/send-with-pubkey {:chat-id       chat-id
                                            :payload       payload
                                            :success-event success-event})))))

(defrecord ContactUpdate [name profile-image address fcm-token]
  message/StatusMessage
  (send [this _ {:keys [db] :as cofx}]
    ;;TODO: here we look for contact which have a :public-key to differentiate
    ;;actual contacts from dapps
    ;;This is not an ideal solution and we should think about a more reliable way
    ;;to do this when we refactor app-db
    (let [contact-public-keys (reduce (fn [acc [_ {:keys [public-key pending?]}]]
                                        (if (and public-key
                                                 (not pending?))
                                          (conj acc public-key)
                                          acc))
                                      #{}
                                      (:contacts/contacts db))
          ;;NOTE: chats with contacts use public-key as chat-id
          send-contact-update-fxs (map #(send-contact-update % this) contact-public-keys)]
      (apply fx/merge cofx send-contact-update-fxs)))
  (validate [this]
    (when (spec/valid? :message/contact-update this)
      this)))

(fx/defn remove-chat-filter
  "Stops the filter for the given chat-id"
  [{:keys [db]} chat-id]
  (when-let [filter (get-in db [:transport/chats chat-id :filter])]
    {:shh/remove-filter filter}))

(fx/defn init-chat
  [cofx chat-id topic]
  (when-not (get-in cofx [:db :transport/chats chat-id])
    (protocol/init-chat cofx
                        {:chat-id chat-id
                         :topic   topic})))

(defrecord NewContactKey [sym-key topic message]
  message/StatusMessage
  (send [this chat-id cofx]
    (let [success-event [:transport/set-contact-message-envelope-hash chat-id]]
      (protocol/send-with-pubkey cofx
                                 {:chat-id       chat-id
                                  :payload       this
                                  :success-event success-event})))
  (receive [this chat-id _ raw-data timestamp {:keys [db] :as cofx}]
    (let [current-sym-key (get-in db [:transport/chats chat-id :sym-key])
          ;; NOTE(yenda) to support concurrent contact request without additional
          ;; interactions we don't save the new key if these conditions are met:
          ;; - the message is a contact request
          ;; - we already have a sym-key
          ;; - this sym-key is first in alphabetical order compared to the new one
          save-key?       (not (and (= ContactRequest (type message))
                                    current-sym-key
                                    (= current-sym-key
                                       (first (sort [current-sym-key sym-key])))))]
      (if save-key?
        (let [on-success (fn [sym-key sym-key-id]
                           (re-frame/dispatch [:contact/add-new-sym-key
                                               {:sym-key-id sym-key-id
                                                :timestamp  timestamp
                                                :sym-key    sym-key
                                                :chat-id    chat-id
                                                :topic      topic
                                                :message    message}]))]
          (fx/merge cofx
                    {:shh/add-new-sym-keys [{:web3       (:web3 db)
                                             :sym-key    sym-key
                                             :on-success on-success}]}
                    (init-chat chat-id topic)
                    ;; in case of concurrent contact request we want
                    ;; to stop the filter for the previous key before
                    ;; dereferrencing it
                    (remove-chat-filter chat-id)))
        ;; if we don't save the key, we read the message directly
        (message/receive message chat-id chat-id raw-data timestamp cofx))))
  (validate [this]
    (when (spec/valid? :message/new-contact-key this)
      this)))
