(ns status-im.data-store.realm.pending-messages
  (:require [status-im.data-store.realm.core :as realm]
            [cljs.reader :refer [read-string]]))

(defn get-all
  []
  (realm/get-all @realm/account-realm :pending-message))

(defn get-all-as-list
  []
  (realm/js-object->clj (get-all)))

(defn get-by-message-id
  [message-id]
  (realm/get-by-field @realm/account-realm :pending-message :message-id message-id))

(defn get-by-chat-id
  [chat-id]
  (realm/get-by-field @realm/account-realm :pending-message :chat-id chat-id))

(defn save
  [pending-message]
  (realm/save @realm/account-realm :pending-message pending-message true))

(defn delete
  [message-id] 
  (realm/delete @realm/account-realm (get-by-message-id message-id)))

(defn delete-all-by-chat-id
  [chat-id]
  (realm/delete @realm/account-realm (get-by-chat-id chat-id)))
