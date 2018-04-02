(ns status-im.data-store.realm.schemas.account.v23.core
  (:require [status-im.data-store.realm.schemas.account.v22.chat :as chat]
            [status-im.data-store.realm.schemas.account.v22.transport :as transport]
            [status-im.data-store.realm.schemas.account.v1.chat-contact :as chat-contact]
            [status-im.data-store.realm.schemas.account.v19.contact :as contact]
            [status-im.data-store.realm.schemas.account.v20.discover :as discover]
            [status-im.data-store.realm.schemas.account.v23.message :as message] 
            [status-im.data-store.realm.schemas.account.v19.request :as request]
            [status-im.data-store.realm.schemas.account.v19.user-status :as user-status]
            [status-im.data-store.realm.schemas.account.v5.contact-group :as contact-group]
            [status-im.data-store.realm.schemas.account.v5.group-contact :as group-contact]
            [status-im.data-store.realm.schemas.account.v8.local-storage :as local-storage]
            [status-im.data-store.realm.schemas.account.v21.browser :as browser]
            [goog.object :as object]
            [taoensso.timbre :as log]
            [cljs.reader :as reader]
            [clojure.string :as string]))

(def schema [chat/schema
             chat-contact/schema
             transport/schema
             contact/schema
             discover/schema
             message/schema 
             request/schema
             user-status/schema
             contact-group/schema
             group-contact/schema
             local-storage/schema
             browser/schema])

(defn update-new-message [new-realm message-id to-clock-value from-clock-value]
  (when-let [message (some-> new-realm
                             (.objects "message")
                             (.filtered (str "message-id = \"" message-id "\""))
                             (aget 0))]
    (aset message "to-clock-value" to-clock-value)
    (aset message "from-clock-value" from-clock-value)))

(defn update-chat-messages [old-realm new-realm chat-id]
  (let [from-clock-value (atom 0)
        to-clock-value   (atom 0)]
    (some-> old-realm
            (.objects "message")
            (.filtered (str "chat-id = \"" chat-id "\""))
            (.sorted "clock-value" false)
            (.map (fn [message _ _]
                    (let [message-id (object/get message "message-id")
                          outgoing?  (boolean (object/get message "outgoing"))]
                      (if outgoing?
                        (update-new-message new-realm message-id (swap! to-clock-value inc) @from-clock-value)
                        (update-new-message new-realm message-id @to-clock-value (swap! from-clock-value inc)))))))))

(defn update-chats [old-realm new-realm]
  (some-> new-realm
          (.objects "chat")
          (.map (fn [chat _ _]
                  (update-chat-messages old-realm new-realm (object/get chat "chat-id"))))))

(defn migration [old-realm new-realm]
  (log/debug "migrating v23 account database: " old-realm new-realm)
  (update-chats old-realm new-realm))
