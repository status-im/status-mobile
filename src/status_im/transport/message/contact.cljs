(ns ^{:doc "Contact request and update API"}
 status-im.transport.message.contact
  (:require [cljs.spec.alpha :as spec]
            [status-im.data-store.transport :as transport-store]
            [status-im.transport.db :as transport.db]
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

(defrecord ContactUpdate [name profile-image address fcm-token]
  protocol/StatusMessage
  (validate [this]
    (when (spec/valid? :message/contact-update this)
      this)))

(fx/defn remove-chat-filter
  "Stops the filter for the given chat-id"
  [{:keys [db]} chat-id]
  (when-let [filter (get-in db [:transport/filters chat-id])]
    {:shh/remove-filter {:chat-id chat-id
                         :filter filter}}))

