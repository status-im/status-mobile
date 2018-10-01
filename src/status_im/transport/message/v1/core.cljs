(ns status-im.transport.message.v1.core
  (:require [status-im.transport.message.core :as message]
            [taoensso.timbre :as log]
            [cljs.spec.alpha :as spec]))

(defrecord GroupMembershipUpdate
           [chat-id membership-updates message]
  message/StatusMessage
  (validate [this]
    (if (spec/valid? :message/group-membership-update this)
      this
      (log/warn "failed group membership validation" (spec/explain :message/group-membership-update this)))))
