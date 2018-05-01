(ns status-im.data-store.realm.messages
  (:require [status-im.data-store.realm.core :as realm])
  (:refer-clojure :exclude [exists?]))

(defn get-all-as-list
  []
  (realm/all-clj (realm/get-all @realm/account-realm :message) :message))

(defn- transform-message [message]
  (update message :user-statuses
          (partial into {}
                   (map (fn [[_ {:keys [whisper-identity status]}]]
                          [whisper-identity (keyword status)])))))

(defn get-by-id
  [message-id]
  (some-> @realm/account-realm
          (realm/get-by-field :message :message-id message-id)
          (realm/single-clj :message)
          transform-message))

(defn get-by-chat-id
  ([chat-id number-of-messages]
   (get-by-chat-id chat-id 0 number-of-messages))
  ([chat-id from number-of-messages]
   (let [messages (-> (realm/get-by-field @realm/account-realm :message :chat-id chat-id)
                      (realm/sorted :timestamp :desc)
                      (realm/page from (+ from number-of-messages))
                      (realm/all-clj :message))]
     (mapv transform-message messages))))

(defn get-message-ids-by-chat-id
  [chat-id]
  (.map (realm/get-by-field @realm/account-realm :message :chat-id chat-id)
        (fn [msg _ _]
          (aget msg "message-id"))))

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

(defn get-last-clock-value
  [chat-id]
  (-> (realm/get-by-field @realm/account-realm :message :chat-id chat-id)
      (realm/sorted :clock-value :desc)
      (realm/single-clj :message)
      :clock-value))

(defn get-unviewed
  [current-public-key]
  (-> @realm/account-realm
      (realm/get-by-fields :user-status :and {:whisper-identity current-public-key
                                              :status           :received})
      (realm/all-clj :user-status)))

(defn exists?
  [message-id]
  (realm/exists? @realm/account-realm :message {:message-id message-id}))

(defn save
  [message]
  (realm/save @realm/account-realm :message message true))

(defn delete
  [message-id]
  (let [current-realm @realm/account-realm]
    (when-let [message (realm/get-by-field current-realm :message :message-id message-id)]
      (realm/delete current-realm message))))

(defn delete-by-chat-id
  [chat-id]
  (let [current-realm @realm/account-realm]
    (realm/delete current-realm
                  (realm/get-by-field current-realm :message :chat-id chat-id))
    (realm/delete current-realm
                  (realm/get-by-field current-realm :user-status :chat-id chat-id))))
