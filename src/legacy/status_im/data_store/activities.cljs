(ns legacy.status-im.data-store.activities
  (:require
    [clojure.set :as set]
    [legacy.status-im.data-store.messages :as messages]
    [status-im2.constants :as constants]
    [status-im2.contexts.shell.activity-center.notification-types :as notification-types]))

(defn mark-notifications-as-read
  [notifications]
  (map #(assoc % :read true) notifications))

(defn pending-contact-request?
  [contact-id {:keys [type author]}]
  (and (= type notification-types/contact-request)
       (= contact-id author)))

(defn parse-notification-counts-response
  [response]
  (reduce-kv (fn [acc k count-number]
               (let [maybe-type (js/parseInt (name k) 10)]
                 (if (notification-types/all-supported maybe-type)
                   (assoc acc maybe-type count-number)
                   acc)))
             {}
             response))

(defn- rpc->type
  [{:keys [type name] :as chat}]
  (condp = type
    notification-types/reply
    (assoc chat
           :chat-name name
           :chat-type constants/private-group-chat-type)

    notification-types/mention
    (assoc chat
           :chat-type constants/private-group-chat-type
           :chat-name name)

    notification-types/private-group-chat
    (assoc chat
           :chat-type  constants/private-group-chat-type
           :chat-name  name
           :public?    false
           :group-chat true)

    notification-types/one-to-one-chat
    (assoc chat
           :chat-type  constants/one-to-one-chat-type
           :chat-name  name
           :public?    false
           :group-chat false)

    chat))

(defn <-rpc
  [item]
  (-> item
      rpc->type
      (set/rename-keys {:lastMessage               :last-message
                        :replyMessage              :reply-message
                        :chatId                    :chat-id
                        :contactVerificationStatus :contact-verification-status
                        :communityId               :community-id
                        :membershipStatus          :membership-status
                        :albumMessages             :album-messages})
      (update :last-message #(when % (messages/<-rpc %)))
      (update :message #(when % (messages/<-rpc %)))
      (update :reply-message #(when % (messages/<-rpc %)))
      (dissoc :chatId)))

(defn <-rpc-seen-state
  [item]
  (:hasSeen item))
