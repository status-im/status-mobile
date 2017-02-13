(ns status-im.data-store.realm.schemas.account.v3.core
  (:require [status-im.data-store.realm.schemas.account.v3.chat :as chat]
            [status-im.data-store.realm.schemas.account.v1.chat-contact :as chat-contact]
            [status-im.data-store.realm.schemas.account.v1.command :as command]
            [status-im.data-store.realm.schemas.account.v3.contact :as contact]
            [status-im.data-store.realm.schemas.account.v1.discover :as discover]
            [status-im.data-store.realm.schemas.account.v1.kv-store :as kv-store]
            [status-im.data-store.realm.schemas.account.v1.message :as message]
            [status-im.data-store.realm.schemas.account.v1.pending-message :as pending-message]
            [status-im.data-store.realm.schemas.account.v1.processed-message :as processed-message]
            [status-im.data-store.realm.schemas.account.v1.request :as request]
            [status-im.data-store.realm.schemas.account.v1.tag :as tag]
            [status-im.data-store.realm.schemas.account.v1.user-status :as user-status]
            [taoensso.timbre :as log]))

(def schema [chat/schema
             chat-contact/schema
             command/schema
             contact/schema
             discover/schema
             kv-store/schema
             message/schema
             pending-message/schema
             processed-message/schema
             request/schema
             tag/schema
             user-status/schema])

(defn migration [old-realm new-realm]
  (log/debug "migrating v3 account database: " old-realm new-realm)
  (chat/migration old-realm new-realm)
  (contact/migration old-realm new-realm))
