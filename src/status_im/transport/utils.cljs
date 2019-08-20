(ns ^{:doc "Utils for transport layer"}
 status-im.transport.utils
  (:require [clojure.string :as string]
            [status-im.ethereum.core :as ethereum]
            [status-im.js-dependencies :as dependencies]))

(defn system-message-id
  [{:keys [from chat-id clock-value]}]
  (ethereum/sha3 (str from chat-id clock-value)))

(defn message-id
  "Get a message-id by appending the hex-encoded pk of the sender to the raw-payload.
  We strip 0x from the payload so web3 understand that the whole thing is to be
  decoded as raw bytes"
  [from raw-payload]
  (ethereum/sha3 (str from (subs raw-payload 2))))

(defn get-topic
  "Get the topic of a group chat or public chat from the chat-id"
  [chat-id]
  (subs (ethereum/sha3 chat-id) 0 10))

(defn extract-enode-id [enode]
  (-> enode
      (string/split #"/")
      (get 2 "")
      (string/split #":")
      (get 0 "")
      (string/split "@")
      (get 0)))

(defn extract-url-components [address]
  (rest (re-matches #"enode://(.*?)@(.*):(.*)" address)))
