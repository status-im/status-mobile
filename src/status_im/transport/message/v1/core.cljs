(ns status-im.transport.message.v1.core
  (:require [status-im.transport.message.core :as message]))

(defrecord GroupMembershipUpdate
           [chat-id chat-name admin participants leaves signature message]
  message/StatusMessage)

(defrecord GroupLeave
           []
  message/StatusMessage)
