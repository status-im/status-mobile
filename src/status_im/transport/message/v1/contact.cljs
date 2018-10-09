(ns ^{:doc "Contact request and update API"}
 status-im.transport.message.v1.contact
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.transport :as transport-store]
            [status-im.transport.message.core :as message]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.fx :as fx]
            [cljs.spec.alpha :as spec]
            [taoensso.timbre :as log]
            [status-im.constants :as constants]))

(defrecord ContactRequest [name profile-image address fcm-token]
  message/StatusMessage
  (send [this chat-id {:keys [db random-id-generator] :as cofx}]
    (fx/merge cofx
              (protocol/init-chat {:chat-id chat-id
                                   :resend? "contact-request"})
              (protocol/send-with-pubkey {:chat-id chat-id
                                          :payload this
                                          :success-event [:transport/set-contact-message-envelope-hash chat-id]})))
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
  (send "no-op, we don't send NewContactKey anymore"
    [this chat-id cofx])
  (receive "for compatibility with old clients, we only care about the message within "
    [this chat-id _ timestamp {:keys [db] :as cofx}]
    (message/receive message chat-id chat-id timestamp cofx))
  (validate [this]
    (when (spec/valid? :message/new-contact-key this)
      this)))
