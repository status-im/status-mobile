(ns status-im.data-store.requests
  (:require [status-im.data-store.realm.requests :as data-store]))

(defn get-all-unanswered
  []
  (data-store/get-all-unanswered))

(defn save
  [request]
  (data-store/save request))

(defn mark-as-answered
  [chat-id message-id]
  (data-store/mark-as-answered chat-id message-id))
