(ns ^{:doc "Definition of the StatusMessage protocol"}
    status-im.transport.message.core)

(defprotocol StatusMessage
  "Protocol for the messages that are sent through the transport layer"
  (send [this chat-id cofx] "Method producing all effects necessary for sending the message record")
  (receive [this chat-id signature cofx] "Method producing all effects necessary for receiving the message record"))
