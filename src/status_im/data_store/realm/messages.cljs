(ns status-im.data-store.realm.messages
  (:require [status-im.data-store.realm.core :as realm])
  (:refer-clojure :exclude [exists?]))


(defn get-all
  []
  (realm/get-all @realm/account-realm :message))

(defn get-all-as-list
  []
  (realm/js-object->clj (get-all)))

(defn get-by-id
  [message-id]
  (when-let [message (realm/get-one-by-field-clj @realm/account-realm :message :message-id message-id)]
    (realm/fix-map message :user-statuses :whisper-identity)))

(defn get-by-chat-id
  "arity-1 returns realm object for queries"
  ([chat-id]
   (realm/get-by-field @realm/account-realm :message :chat-id chat-id))
  ([chat-id number-of-messages]
   (get-by-chat-id chat-id 0 number-of-messages))
  ([chat-id from number-of-messages]
   (let [messages (-> (realm/get-by-field @realm/account-realm :message :chat-id chat-id)
                      (realm/sorted :timestamp :desc)
                      (realm/page from (+ from number-of-messages))
                      realm/js-object->clj)]
     (mapv #(realm/fix-map % :user-statuses :whisper-identity) messages))))

(defn get-count-by-chat-id
  [chat-id]
  (realm/get-count (get-by-chat-id chat-id)))

(defn get-by-fields
  [fields from number-of-messages]
  (-> (realm/get-by-fields @realm/account-realm :message :and fields)
      (realm/sorted :timestamp :desc)
      (realm/page from (+ from number-of-messages))
      realm/js-object->clj))

(defn get-last-message
  [chat-id]
  (-> (realm/get-by-field @realm/account-realm :message :chat-id chat-id)
      (realm/sorted :clock-value :desc)
      (realm/single-clj)))

(defn get-unviewed
  []
  (-> @realm/account-realm
      (realm/get-by-fields :message :and {:outgoing       false
                                          :message-status nil})
      realm/js-object->clj))

(defn exists?
  [message-id]
  (realm/exists? @realm/account-realm :message {:message-id message-id}))

(defn save
  [message]
  (let [message (update message :user-statuses #(if % % []))]
    (realm/save @realm/account-realm :message message true)))

(defn delete-by-chat-id
  [chat-id]
  (realm/delete @realm/account-realm
                (get-by-chat-id chat-id)))
