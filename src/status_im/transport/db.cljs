(ns ^{:doc "DB spec and utils for the transport layer"}
 status-im.transport.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]
            status-im.ui.screens.contacts.db
            [clojure.string :as s]))

;; required
(spec/def ::ack (spec/coll-of string? :kind vector?))
(spec/def ::seen (spec/coll-of string? :kind vector?))
(spec/def ::pending-ack (spec/coll-of string? :kind vector?))
(spec/def ::pending-send (spec/coll-of string? :kind vector?))
(spec/def ::topic string?)
(spec/def ::fetch-history? boolean?)
(spec/def ::resend? (spec/nilable #{"contact-request" "contact-request-confirmation" "contact-update"}))

;; optional
(spec/def ::sym-key-id (spec/nilable string?))
;;TODO (yenda) remove once go implements persistence
(spec/def ::sym-key (spec/nilable string?))
(spec/def ::filter any?)

(spec/def :transport/chat (allowed-keys :req-un [::ack ::seen ::pending-ack ::pending-send ::topic ::fetch-history?]
                                        :opt-un [::sym-key-id ::sym-key ::filter ::resend?]))

(spec/def :transport/chats (spec/map-of :global/not-empty-string :transport/chat))
(spec/def :transport/discovery-filter (spec/nilable any?))

(defn create-chat
  "Initialize datastructure for chat representation at the transport level
  Currently only :topic is actually used"
  [{:keys [topic resend?]}]
  {:ack                   []
   :seen                  []
   :pending-ack           []
   :pending-send          []
   :fetch-history?        true
   :resend?               resend?
   :topic                 topic})

(spec/def ::profile-image :contact/photo-path)

(spec/def :chat/name (spec/nilable string?))

(spec/def :group-chat/admin :global/public-key)
(spec/def :group-chat/participants (spec/coll-of :global/public-key :kind set?))
(spec/def :group-chat/signature string?)

(spec/def :message.content/text (spec/and string? (complement s/blank?)))
(spec/def :message.content/response-to string?)
(spec/def :message.content/command-path (spec/tuple string? (spec/coll-of (spec/or :scope keyword? :chat-id string?) :kind set? :min-count 1)))
(spec/def :message.content/params (spec/map-of keyword? any?))

(spec/def ::content-type #{"text/plain" "command" "command-request"})
(spec/def ::message-type #{:group-user-message :public-group-user-message :user-message})
(spec/def ::clock-value (spec/nilable pos-int?))
(spec/def ::timestamp (spec/nilable pos-int?))

(spec/def :message/id string?)
(spec/def :message/ids (spec/coll-of :message/id :kind set?))

(spec/def ::message (spec/or :message/contact-request :message/contact-request
                             :message/contact-update :message/contact-update
                             :message/contact-request-confirmed :message/contact-request-confirmed
                             :message/message :message/message
                             :message/message-seen :message/message-seen
                             :message/group-membership-update :message/group-membership-update))

(spec/def :message/contact-request (spec/keys :req-un [:contact/name ::profile-image :contact/address :contact/fcm-token]))
(spec/def :message/contact-update (spec/keys :req-un [:contact/name ::profile-image :contact/address :contact/fcm-token]))
(spec/def :message/contact-request-confirmed (spec/keys :req-un [:contact/name ::profile-image :contact/address :contact/fcm-token]))
(spec/def :message/new-contact-key (spec/keys :req-un [::sym-key ::topic ::message]))

(spec/def :message/message-seen (spec/keys :req-un [:message/ids]))

(spec/def :message/group-membership-update (spec/keys :req-un [:chat/chat-id :chat/name :group-chat/admin :group-chat/participants :group-chat/signature :message/message]))

(spec/def :message/message-common (spec/keys :req-un [::content-type ::message-type ::clock-value ::timestamp]))
(spec/def :message.text/content (spec/keys :req-un [:message.content/text]
                                           :req-opt [:message.content/response-to]))
(spec/def :message.command/content (spec/keys :req-un [:message.content/command-path :message.content/params]))
(spec/def :message.buidl/content map?)

(defmulti content-type :content-type)

(defmethod content-type "command" [_]
  (spec/merge :message/message-common
              (spec/keys :req-un [:message.command/content])))

(defmethod content-type "buidl" [_]
  (spec/merge :message/message-common
              (spec/keys :req-un [:message.buidl/content])))

(defmethod content-type "command-request" [_]
  (spec/merge :message/message-common
              (spec/keys :req-un [:message.command/content])))

(defmethod content-type :default [_]
  (spec/merge :message/message-common
              (spec/keys :req-un [:message.text/content])))

(spec/def :message/message (spec/multi-spec content-type :content-type))
