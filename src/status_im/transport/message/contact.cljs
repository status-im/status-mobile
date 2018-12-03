(ns ^{:doc "Contact request and update API"}
 status-im.transport.message.contact
  (:require [cljs.spec.alpha :as spec]
            [status-im.data-store.transport :as transport-store]
            [status-im.transport.message.protocol :as protocol]
            [status-im.utils.fx :as fx]))

(defrecord ContactRequest [name profile-image address fcm-token]
  protocol/StatusMessage
  (validate [this]
    (when (spec/valid? :message/contact-request this)
      this)))

(defrecord ContactRequestConfirmed [name profile-image address fcm-token]
  protocol/StatusMessage
  (validate [this]
    (when (spec/valid? :message/contact-request-confirmed this)
      this)))

(fx/defn send-contact-update
  [{:keys [db] :as cofx} chat-id payload]
  (when-let [chat (get-in cofx [:db :transport/chats chat-id])]
    (let [updated-chat  (assoc chat :resend? "contact-update")
          tx            [(transport-store/save-transport-tx {:chat-id chat-id
                                                             :chat    updated-chat})]
          success-event [:transport/contact-message-sent chat-id]]
      (fx/merge cofx
                {:db (assoc-in db
                               [:transport/chats chat-id :resend?]
                               "contact-update")
                 :data-store/tx tx}
                (protocol/send-with-pubkey {:chat-id       chat-id
                                            :payload       payload
                                            :success-event success-event})))))

(defrecord ContactUpdate [name profile-image address fcm-token]
  protocol/StatusMessage
  (send [this _ {:keys [db] :as cofx}]
    (let [contact-public-keys (reduce (fn [acc [_ {:keys [public-key dapp? pending?]}]]
                                        (if (and (not dapp?)
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
  (when-let [filter (get-in db [:transport/filters chat-id])]
    {:shh/remove-filter {:chat-id chat-id
                         :filter filter}}))

