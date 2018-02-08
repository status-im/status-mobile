(ns status-im.protocol.message
  (:require [cljs.spec.alpha :as s]
            [status-im.protocol.web3.filtering :as web3.filtering]))

(s/def :message/ttl (s/and int? pos?))
(s/def :message/from string?)
(s/def :message/sig :message/from)
(s/def :message/privateKeyID (s/nilable string?))
(s/def :message/pub-key (s/nilable string?))
(s/def :message/sym-key-password (s/nilable string?))
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


(def discover-topic-prefix "status-discover-")
(def discover-topic "0xbeefdead")

(def ping-topic "0x01010202")

(def ttl 10000)

(def protocol-version 0)

(defn message-type-options [message-type]
  (get message-type
       {:contact/request {:require-ack? true}
        :contact/message {:require-ack? true}}
       {}))

(defmulti send-options :message-type)

(defmethod send-options :system/ping
  [{:keys [message-type db chat-id status-message]}]
  (let [{:keys [current-public-key]} db]
    {:message {:sig current-public-key
               :pubKey chat-id
               :ttl ttl
               :payload [0 :system/ping status-message]
               :topic ping-topic}}))

(defmethod send-options :contact/request
  [{:keys [message-type db chat-id status-message]}]
  (let [{:accounts/keys [account]} db]
    {:message {:sig (:public-key account)
               :pubKey chat-id
               :ttl ttl
               :payload [0 :contact/request status-message]
               :topic web3.filtering/status-topic}}))

(defmethod send-options :contact/message
  [{:keys [message-type db chat-id status-message]}]
  (let [{:accounts/keys [account]} db]
    {:message {:sig (:public-key account)
               :pubKey chat-id
               :ttl ttl
               :payload [0 :contact/message status-message]
               :topic web3.filtering/status-topic}}))
