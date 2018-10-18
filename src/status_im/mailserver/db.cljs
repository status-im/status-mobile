(ns status-im.mailserver.db
  (:require [status-im.mailserver.core :as mailserver]
            [cljs.spec.alpha :as spec]))

(spec/def :mailserver/state (spec/nilable #{:disconnected :connecting :added :connected :error}))
(spec/def :mailserver/current-id (spec/nilable keyword?))
(spec/def :mailserver/address (spec/and string? mailserver/valid-enode-address?))
(spec/def :mailserver/name :global/not-empty-string)
(spec/def :mailserver/id keyword?)
(spec/def :mailserver/user-defined boolean?)
(spec/def :mailserver/password :global/not-empty-string)
(spec/def :mailserver/sym-key-id string?)
(spec/def :mailserver/generating-sym-key? boolean?)
(spec/def :mailserver/mailserver (spec/keys :req-un [:mailserver/address :mailserver/name :mailserver/id]
                                            :opt-un [:mailserver/sym-key-id
                                                     :mailserver/generating-sym-key?
                                                     :mailserver/user-defined
                                                     :mailserver/password]))

(spec/def :mailserver/mailservers (spec/nilable (spec/map-of keyword? (spec/map-of :mailserver/id :mailserver/mailserver))))

(spec/def :request/from pos-int?)
(spec/def :request/to pos-int?)
(spec/def :request/attemps int?)
(spec/def :request/cursor :global/not-empty-string)

(spec/def :mailserver.topic/last-request pos-int?)
(spec/def :mailserver.topic/started-at pos-int?)
(spec/def :mailserver.topic/chat-id (spec/or :keyword keyword?
                                             :chat-id :global/not-empty-string))
(spec/def :mailserver.topic/chat-ids (spec/coll-of :mailserver.topic/chat-id
                                                   :kind set?
                                                   :min-count 1))

(spec/def :mailserver/topic (spec/keys :req-un [:mailserver.topic/last-request
                                                :mailserver.topic/chat-ids]))
(spec/def :mailserver/request-to :request/to)
(spec/def :mailserver/connection-checks pos-int?)
(spec/def :mailserver/topics (spec/map-of :global/not-empty-string :mailserver/topic))
(spec/def :mailserver/current-request (spec/keys :req-un [:request/from :request/to ::topics]
                                                 :opt-un [:request/attemps]))
(spec/def :mailserver/pending-requests integer?)
