(ns ^{:doc "Utils for transport layer"}
 status-im.transport.utils
  (:require [clojure.string :as string]
            [status-im.js-dependencies :as dependencies]))

(defn from-utf8 [s]
  (.fromUtf8 (.-prototype (dependencies/Web3)) s))

(defn to-ascii [s]
  (.toAscii (.-prototype (dependencies/Web3)) s))

(defn to-utf8 [s]
  (try
    (.toUtf8 (.-prototype (dependencies/Web3)) (str s))
    (catch :default err nil)))

(defn sha3 [s]
  (.sha3 (.-prototype (dependencies/Web3)) s))

(defn old-message-id
  [message]
  (sha3 (pr-str message)))

(defn system-message-id
  [{:keys [from chat-id clock-value]}]
  (sha3 (str from chat-id clock-value)))

(defn message-id
  "Get a message-id"
  [from raw-payload]
  (sha3 (str from (sha3 raw-payload))))

(defn get-topic
  "Get the topic of a group chat or public chat from the chat-id"
  [chat-id]
  (subs (sha3 chat-id) 0 10))

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
