(ns status-im.data-store.chats
  (:require [status-im.data-store.realm.chats :as data-store])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  []
  (data-store/get-all-active))

(defn get-inactive-ids
  []
  (data-store/get-inactive-ids))

(defn get-by-id
  [id]
  (data-store/get-by-id id))

(defn exists?
  [chat-id]
  (data-store/exists? chat-id))

(defn save
  [{:keys [chat-id] :as chat}]
  (data-store/save chat (data-store/exists? chat-id)))

(defn delete
  [chat-id]
  (data-store/delete chat-id))

(defn set-inactive
  [chat-id]
  (data-store/set-inactive chat-id))

(defn get-contacts
  [chat-id]
  (data-store/get-contacts chat-id))

(defn add-contacts
  [chat-id identities]
  (data-store/add-contacts chat-id identities))

(defn remove-contacts
  [chat-id identities]
  (data-store/remove-contacts chat-id identities))

(defn save-property
  [chat-id property-name value]
  (data-store/save-property chat-id property-name value))

(defn get-property
  [chat-id property-name]
  (data-store/get-property chat-id property-name))

(defn removed-at
  [chat-id]
  (get-property chat-id :removed-at))

(defn get-active-group-chats
  []
  (data-store/get-active-group-chats))

(defn set-active
  [chat-id active?]
  (save-property chat-id :is-active active?))
