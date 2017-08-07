(ns status-im.data-store.realm.schemas.account.v15.core
  (:require [status-im.data-store.realm.schemas.account.v11.chat :as chat]
            [status-im.data-store.realm.schemas.account.v1.chat-contact :as chat-contact]
            [status-im.data-store.realm.schemas.account.v6.command :as command]
            [status-im.data-store.realm.schemas.account.v9.command-parameter :as command-parameter]
            [status-im.data-store.realm.schemas.account.v15.contact :as contact]
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
            [taoensso.timbre :as log]))

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

(defn migration [old-realm new-realm]
  (log/debug "migrating v15 account database: " old-realm new-realm))