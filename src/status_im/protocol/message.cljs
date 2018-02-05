(ns status-im.protocol.message
  (:require [cljs.spec.alpha :as s]
            [taoensso.timbre :as log]
            [status-im.utils.random :as random]
            [status-im.protocol.web3.filtering :as web3.fitering]
            [status-im.protocol.web3.utils :as web3.utils]))

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







(s/def :send-online/message
  (s/merge :protocol/message
           (s/keys :req-un [:message/keypair])))
(s/def :send-online/options
  (s/keys :req-un [:options/web3 :send-online/message]))

(def discover-topic-prefix "status-discover-")
(def discover-topic "0xbeefdead")

(defn- make-discover-topic [identity]
  (str discover-topic-prefix identity))



(def ttl 10000)

(defmulti post :type)

(defmethod post :online
  [{:keys [sym-key-id]}]
  {:sig      identity
   :symKeyID sym-key-id
   :ttl      ttl
   :payload  {:type :online
              :message-id (random/id)
              :content {:timestamp (web3.utils/timestamp)}
              :requires-ack? false}
   :topic    web3.fitering/status-topic})

(defmethod post :contact-request
  [{:keys [account contact fcm-token]}]
  (let [{:keys [name photo-path address status]} account]
    {:shh/post {:web3 web3
                :message {:sig (:whisper-id account)
                          :pubKey (:whisper-id contact)
                          :ttl ttl
                          :payload {:type :contact-request
                                    :message-id (random/id)
                                    :contact {:name          name
                                              :profile-image photo-path
                                              :address       address
                                              :status        status
                                              :fcm-token     fcm-token}
                                    :timestamp (web3.utils/timestamp)
                                    :require-ack? true}
                          :topic web3.fitering/status-topic}
                :on-success #(log/debug :contact-request-sent)
                :on-error   #(log/error :contact-request-failed %)}}))
