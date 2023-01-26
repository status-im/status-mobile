(ns status-im.data-store.activities
  (:require [clojure.set :as set]
            [status-im2.constants :as constants]
            [status-im.data-store.messages :as messages]
            [status-im2.contexts.activity-center.notification-types :as notification-types]))

(defn- rpc->type
  [{:keys [type name] :as chat}]
  (case type
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
                        :membershipStatus          :membership-status})
      (update :last-message #(when % (messages/<-rpc %)))
      (update :message #(when % (messages/<-rpc %)))
      (update :reply-message #(when % (messages/<-rpc %)))
      (dissoc :chatId)))
