(ns status-im.protocol.message
  (:require [cljs.spec.alpha :as s]))

(s/def :message/ttl (s/and int? pos?))
(s/def :message/from string?)
(s/def :message/sig :message/from)
(s/def :message/privateKeyID (s/nilable string?))
(s/def :message/pub-key (s/nilable string?))
(s/def :message/sym-key-id (s/nilable string?))
(s/def :message/topic string?)
(s/def :message/to (s/nilable string?))
(s/def :message/message-id string?)
(s/def :message/requires-ack? boolean?)
(s/def :keypair/private string?)
(s/def :keypair/public string?)
(s/def :message/keypair (s/keys :req-un [:keypair/private
                                         :keypair/public]))
(s/def :message/topics (s/* string?))

(s/def :payload/content (s/or :string-message string?
                              :command map?))
(s/def :payload/content-type string?)
(s/def :payload/timestamp (s/and int? pos?))
(s/def :payload/new-keypair :message/keypair)

(s/def :group-message/type
  #{:public-group-message :group-message :group-invitation :add-group-identity
    :remove-group-identity :leave-group :update-group})

(s/def :discover-message/type #{:online :status :discover :contact-request :update-keys})

(s/def :message/type
  (s/or :group :group-message/type
        :discover :discover-message/type
        :user #{:message}))

(s/def :message/payload
  (s/keys :opt-un [:message/type
                   :payload/content
                   :payload/content-type
                   :payload/new-keypair
                   :payload/timestamp]))

(s/def :protocol/message
  (s/keys :req-un [:message/from :message/message-id]
          :opt-un [:message/to :message/topics :message/requires-ack?
                   :message/keypair :message/ttl :message/payload]))

(s/def :chat-message/payload
  (s/keys :req-un [:payload/content :payload/content-type :payload/timestamp]))

(s/def :options/web3 #(not (nil? %)))
