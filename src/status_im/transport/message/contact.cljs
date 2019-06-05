(ns ^{:doc "Contact request and update API"}
 status-im.transport.message.contact
  (:require [cljs.spec.alpha :as spec]
            [status-im.transport.message.protocol :as protocol]
            [status-im.utils.fx :as fx]))

(defrecord ContactRequest [name profile-image address fcm-token device-info]
  protocol/StatusMessage
  (validate [this]
    (when (spec/valid? :message/contact-request this)
      this)))

(defrecord ContactRequestConfirmed [name profile-image address fcm-token device-info]
  protocol/StatusMessage
  (validate [this]
    (when (spec/valid? :message/contact-request-confirmed this)
      this)))

(defrecord ContactUpdate [name profile-image address fcm-token device-info]
  protocol/StatusMessage
  (validate [this]
    (when (spec/valid? :message/contact-update this)
      this)))
