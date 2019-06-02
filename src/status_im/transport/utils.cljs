(ns ^{:doc "Utils for transport layer"}
 status-im.transport.utils
  (:require [clojure.string :as string]
            [status-im.ethereum.core :as ethereum]
            [status-im.js-dependencies :as dependencies]))

(defn old-message-id
  [message]
  (ethereum/sha3 (pr-str message)))

(defn system-message-id
  [{:keys [from chat-id clock-value]}]
  (ethereum/sha3 (str from chat-id clock-value)))

(defn message-id
  "Get a message-id"
  [from raw-payload]
  (ethereum/sha3 (str from (ethereum/sha3 raw-payload))))

(defn get-topic
  "Get the topic of a group chat or public chat from the chat-id"
  [chat-id]
  (subs (ethereum/sha3 chat-id) 0 10))

(defn shh [web3]
  (.-shh web3))

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
