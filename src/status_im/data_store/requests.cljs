(ns status-im.data-store.requests
  (:require [status-im.data-store.realm.requests :as data-store]))

(defn get-all
  []
  (data-store/get-all-as-list))

(defn get-open-by-chat-id
  [chat-id]
  (data-store/get-open-by-chat-id chat-id))

(defn save
  [request]
  (data-store/save request))

(defn save-all
  [requests]
  (data-store/save-all requests))

(defn mark-as-answered
  [chat-id message-id]
  (data-store/mark-as-answered chat-id message-id))
