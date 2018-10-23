(ns ^{:doc "Utils for transport layer"}
 status-im.transport.utils
  (:require [clojure.string :as string]
            [status-im.js-dependencies :as dependencies]))

(defn from-utf8 [s]
  (.fromUtf8 dependencies/Web3.prototype s))

(defn to-ascii [s]
  (.toAscii dependencies/Web3.prototype s))

(defn to-utf8 [s]
  (try
    (.toUtf8 dependencies/Web3.prototype (str s))
    (catch :default err nil)))

(defn sha3 [s]
  (.sha3 dependencies/Web3.prototype s))

(defn message-id
  "Get a message-id"
  [message]
  (sha3 (pr-str message)))

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
