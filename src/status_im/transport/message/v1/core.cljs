(ns status-im.transport.message.v1.core
  (:require [status-im.transport.message.core :as message]))

(defrecord Message
           [content content-type message-type clock-value timestamp]
  message/StatusMessage)

(defrecord GroupMembershipUpdate
           [chat-id chat-name admin participants leaves signature message]
  message/StatusMessage)

(defrecord MessagesSeen
           [message-ids]
  message/StatusMessage)

(defrecord ContactRequest
           [name profile-image address fcm-token]
  message/StatusMessage)

(defrecord ContactRequestConfirmed
           [name profile-image address fcm-token]
  message/StatusMessage)

(defrecord ContactUpdate
           [name profile-image address fcm-token]
  message/StatusMessage)

(defrecord NewContactKey
           [sym-key topic message]
  message/StatusMessage)

(defrecord GroupLeave
           []
  message/StatusMessage)
