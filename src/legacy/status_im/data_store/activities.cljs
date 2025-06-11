(ns legacy.status-im.data-store.activities
  (:require
    [clojure.set :as set]
    [clojure.string :as string]
    [legacy.status-im.data-store.messages :as messages]
    [status-im.constants :as constants]
    [status-im.contexts.shell.activity-center.notification-types :as notification-types]))

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

(defn strip-html
  [html]
  (-> html
      ;; Replace block-level tags with newlines
      (clojure.string/replace #"</?(h[1-6]|p|div|br|li|ul|ol|section|article|blockquote)[^>]*>" "\n")
      ;; Remove all remaining tags
      (clojure.string/replace #"<[^>]+>" "")
      ;; Decode common HTML entities
      (clojure.string/replace #"&nbsp;" " ")
      (clojure.string/replace #"&amp;" "&")
      ;; Collapse multiple newlines into one
      (clojure.string/replace #"\n+" "\n")
      ;; Trim spaces at start/end of lines
      (clojure.string/replace #"[ \t]+\n" "\n")
      (clojure.string/replace #"\n[ \t]+" "\n")
      clojure.string/trim))

(defn <-rpc
  [item]
  (-> item
      rpc->type
      (set/rename-keys {:lastMessage               :last-message
                        :replyMessage              :reply-message
                        :chatId                    :chat-id
                        :contactVerificationStatus :contact-verification-status
                        :communityId               :community-id
                        :installationId            :installation-id
                        :membershipStatus          :membership-status
                        :albumMessages             :album-messages
                        :newsImageUrl              :news-image-url
                        :newsTitle                 :news-title
                        :newsDescription           :news-description
                        :newsContent               :news-content
                        :newsLink                  :news-link
                        :newsLinkLabel             :news-link-label})
      (update :last-message #(when % (messages/<-rpc %)))
      (update :message #(when % (messages/<-rpc %)))
      (update :reply-message #(when % (messages/<-rpc %)))
      (update :news-description #(when % (strip-html %)))
      (update :news-title #(when % (strip-html %)))
      (update :news-content #(when % (strip-html %)))
      (dissoc :chatId)))

(defn <-rpc-seen-state
  [item]
  (:hasSeen item))
