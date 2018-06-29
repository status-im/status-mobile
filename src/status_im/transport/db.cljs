(ns ^{:doc "DB spec and utils for the transport layer"}
 status-im.transport.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]))

;; required
(spec/def ::ack (spec/coll-of string? :kind vector?))
(spec/def ::seen (spec/coll-of string? :kind vector?))
(spec/def ::pending-ack (spec/coll-of string? :kind vector?))
(spec/def ::pending-send (spec/coll-of string? :kind vector?))
(spec/def ::topic string?)
(spec/def ::fetch-history? boolean?)
(spec/def ::resend? (spec/nilable #{"contact-request" "contact-request-confirmation" "contact-update"}))

;; optional
(spec/def ::sym-key-id string?)
;;TODO (yenda) remove once go implements persistence
(spec/def ::sym-key string?)
(spec/def ::filters (spec/coll-of any?))

(spec/def :transport/chat (allowed-keys :req-un [::ack ::seen ::pending-ack ::pending-send ::topic ::fetch-history?]
                                        :opt-un [::sym-key-id ::sym-key ::filters ::resend? ::one-to-one]))

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
   :one-to-one            false
   :fetch-history?        true
   :resend?               resend?
   :topic                 topic})
