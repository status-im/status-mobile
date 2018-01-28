(ns status-im.data-store.realm.messages
  (:require [status-im.data-store.realm.core :as realm])
  (:refer-clojure :exclude [exists?]))


(defn get-all
  []
  (realm/get-all @realm/account-realm :message))

(defn get-all-as-list
  []
  (realm/js-object->clj (get-all)))

(defn- transform-message [message]
  (update message :user-statuses
          (partial into {}
                   (map (fn [[_ {:keys [whisper-identity status]}]]
                          [whisper-identity (keyword status)])))))

(defn get-by-id
  [message-id]
  (some-> (realm/get-one-by-field-clj @realm/account-realm :message :message-id message-id)
          transform-message))

(defn get-by-chat-id
  ([chat-id number-of-messages]
   (get-by-chat-id chat-id 0 number-of-messages))
  ([chat-id from number-of-messages]
   (let [messages (-> (realm/get-by-field @realm/account-realm :message :chat-id chat-id)
                      (realm/sorted :timestamp :desc)
                      (realm/page from (+ from number-of-messages))
                      realm/js-object->clj)]
     (mapv transform-message messages))))

(defn get-stored-message-ids
  []
  (let [chat-id->message-id (volatile! {})]
    (-> @realm/account-realm
        (.objects "message")
        (.map (fn [msg _ _]
                (vswap! chat-id->message-id
                        #(update %
                                 (aget msg "chat-id")
                                 (fnil conj #{})
                                 (aget msg "message-id"))))))
    @chat-id->message-id))

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
  [current-public-key]
  (-> @realm/account-realm
      (realm/get-by-fields :user-status :and {:whisper-identity current-public-key
                                              :status           :received})
      realm/js-object->clj))

(defn exists?
  [message-id]
  (realm/exists? @realm/account-realm :message {:message-id message-id}))

(defn save
  [message]
  (realm/save @realm/account-realm :message message true))

(defn delete-by-chat-id
  [chat-id]
  (let [current-realm @realm/account-realm]
    (realm/delete current-realm
                  (realm/get-by-field current-realm :message :chat-id chat-id))
    (realm/delete current-realm
                  (realm/get-by-field current-realm :user-status :chat-id chat-id))))
