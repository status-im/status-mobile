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

;; optional
(spec/def ::sym-key-id string?)
;;TODO (yenda) remove once go implements persistence
(spec/def ::sym-key string?)
(spec/def ::filter any?)

(spec/def :transport/chat (allowed-keys :req-un [::ack ::seen ::pending-ack ::pending-send ::topic]
                                        :opt-un [::sym-key-id ::sym-key ::filter]))

(spec/def :transport/chats (spec/map-of :global/not-empty-string :transport/chat))
(spec/def :transport/discovery-filter (spec/nilable any?))


(defn create-chat
  "Initialize datastructure for chat representation at the transport level
  Currently only :topic is actually used"
  [topic]
  {:ack          []
   :seen         []
   :pending-ack  []
   :pending-send []
   :topic        topic})
