(ns status-im.data-store.realm.schemas.account.v16.core
  (:require [status-im.data-store.realm.schemas.account.v11.chat :as chat]
            [status-im.data-store.realm.schemas.account.v1.chat-contact :as chat-contact]
            [status-im.data-store.realm.schemas.account.v6.command :as command]
            [status-im.data-store.realm.schemas.account.v9.command-parameter :as command-parameter]
            [status-im.data-store.realm.schemas.account.v16.contact :as contact]
            [status-im.data-store.realm.schemas.account.v1.discover :as discover]
            [status-im.data-store.realm.schemas.account.v1.kv-store :as kv-store]
            [status-im.data-store.realm.schemas.account.v10.message :as message]
            [status-im.data-store.realm.schemas.account.v12.pending-message :as pending-message]
            [status-im.data-store.realm.schemas.account.v1.processed-message :as processed-message]
            [status-im.data-store.realm.schemas.account.v15.request :as request]
            [status-im.data-store.realm.schemas.account.v1.tag :as tag]
            [status-im.data-store.realm.schemas.account.v1.user-status :as user-status]
            [status-im.data-store.realm.schemas.account.v5.contact-group :as contact-group]
            [status-im.data-store.realm.schemas.account.v5.group-contact :as group-contact]
            [status-im.data-store.realm.schemas.account.v8.local-storage :as local-storage]
            [status-im.data-store.realm.schemas.account.v13.handler-data :as handler-data]
            [goog.object :as object]
            [taoensso.timbre :as log]
            [cljs.reader :as reader]))

(def schema [chat/schema
             chat-contact/schema
             command/schema
             command-parameter/schema
             contact/schema
             discover/schema
             kv-store/schema
             message/schema
             pending-message/schema
             processed-message/schema
             request/schema
             tag/schema
             user-status/schema
             contact-group/schema
             group-contact/schema
             local-storage/schema
             handler-data/schema])

(defn chat-by-id [realm chat-id]
  (some-> realm
          (.objects "chat")
          (.filtered (str "chat-id = \"" chat-id "\""))
          (aget 0)))

(defn migrate-commands [realm content-type]
  (some-> realm
          (.objects "message")
          (.filtered (str "content-type = \"" content-type "\""))
          (.map (fn [object _ _]
                  (let [group-id (object/get object "group-id")
                        {:keys [bot] :as content} (reader/read-string (object/get object "content"))]
                    (when-not bot
                      (let [chat-id  (object/get object "chat-id")
                            chat     (chat-by-id realm chat-id)
                            group?   (object/get chat "group-chat")
                            bot-name (if group?
                                       "transactor-group"
                                       "transactor-personal")
                            content' (assoc content :bot bot-name)]
                        (aset object "content" (pr-str content')))))))))

(defn migration [old-realm new-realm]
  (log/debug "migrating v16 account database: " old-realm new-realm)
  (when-let [wallet-chat (chat-by-id new-realm "wallet")]
    (.delete new-realm wallet-chat))
  (when-let [wallet-contact (some-> new-realm
                                    (.objects "contact")
                                    (.filtered (str "whisper-identity = \"wallet\""))
                                    (aget 0))]
    (.delete new-realm wallet-contact))
  (migrate-commands new-realm "command-request")
  (migrate-commands new-realm "command"))
