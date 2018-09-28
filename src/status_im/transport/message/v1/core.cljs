(ns status-im.transport.message.v1.core
  (:require [status-im.transport.message.core :as message]
            [cljs.spec.alpha :as spec]))

(defrecord GroupMembershipUpdate
           [chat-id chat-name admin participants leaves version signature message]
  message/StatusMessage
  (validate [this]
    (when (spec/valid? :message/group-membership-update this)
      this)))

(defrecord GroupLeave
           []
  message/StatusMessage
  (validate [this] this))
