(ns status-im.transport.message.group-chat
  (:require [cljs.spec.alpha :as spec]
            [status-im.transport.message.protocol :as protocol]
            [taoensso.timbre :as log]))

(defrecord GroupMembershipUpdate
           [chat-id membership-updates message]
  protocol/StatusMessage
  (validate [this]
    (if (spec/valid? :message/group-membership-update this)
      this
      (log/warn "failed group membership validation" (spec/explain :message/group-membership-update this)))))
