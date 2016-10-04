(ns status-im.data-store.realm.messages
  (:require [status-im.data-store.realm.core :as realm])
  (:refer-clojure :exclude [exists?]))


(defn get-all
  []
  (realm/get-all @realm/account-realm :message))

(defn get-all-as-list
  []
  (-> (get-all)
      realm/realm-collection->list))

(defn get-by-id
  [message-id]
  (realm/get-one-by-field-clj @realm/account-realm :message :message-id message-id))

(defn get-by-chat-id
  ([chat-id]
   (realm/get-by-field @realm/account-realm :message :chat-id chat-id))
  ([chat-id number-of-messages]
   (get-by-chat-id chat-id 0 number-of-messages))
  ([chat-id from number-of-messages]
   (-> (realm/get-by-field @realm/account-realm :message :chat-id chat-id)
       (realm/sorted :timestamp :desc)
       (realm/page from (+ from number-of-messages))
       (realm/realm-collection->list))))

(defn get-last-message
  [chat-id]
  (-> (realm/get-by-field @realm/account-realm :message :chat-id chat-id)
      (realm/sorted :timestamp :desc)
      (realm/single)
      (js->clj :keywordize-keys true)))

(defn get-unviewed
  []
  (-> (realm/get-by-fields @realm/account-realm :message :and {:outgoing       false
                                                               :message-status nil})
      (realm/realm-collection->list)))

(defn exists?
  [message-id]
  (realm/exists? @realm/account-realm :message {:message-id message-id}))

(defn save
  [message]
  (realm/save @realm/account-realm :message message true))

(defn delete-by-chat-id
  [chat-id]
  (realm/write @realm/account-realm
               #(realm/delete @realm/account-realm
                              (get-by-chat-id chat-id))))