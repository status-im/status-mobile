(ns status-im.data-store.realm.schemas.account.v1.core
  (:require [status-im.data-store.realm.schemas.account.v1.chat :as chat]
            [status-im.data-store.realm.schemas.account.v1.chat-contact :as chat-contact]
            [status-im.data-store.realm.schemas.account.v1.command :as command]
            [status-im.data-store.realm.schemas.account.v1.contact :as contact]
            [status-im.data-store.realm.schemas.account.v1.discovery :as discovery]
            [status-im.data-store.realm.schemas.account.v1.kv-store :as kv-store]
            [status-im.data-store.realm.schemas.account.v1.message :as message]
            [status-im.data-store.realm.schemas.account.v1.pending-message :as pending-message]
            [status-im.data-store.realm.schemas.account.v1.request :as request]
            [status-im.data-store.realm.schemas.account.v1.tag :as tag]
            [status-im.data-store.realm.schemas.account.v1.user-status :as user-status]
            [taoensso.timbre :as log]))

(def schema [chat/schema
             chat-contact/schema
             command/schema
             contact/schema
             discovery/schema
             kv-store/schema
             message/schema
             pending-message/schema
             request/schema
             tag/schema
             user-status/schema])

(defn migration [old-realm new-realm]
  (log/debug "migrating v1 account database: " old-realm new-realm)
  (chat/migration old-realm new-realm)
  (chat-contact/migration old-realm new-realm)
  (command/migration old-realm new-realm)
  (contact/migration old-realm new-realm)
  (discovery/migration old-realm new-realm)
  (kv-store/migration old-realm new-realm)
  (message/migration old-realm new-realm)
  (pending-message/migration old-realm new-realm)
  (request/migration old-realm new-realm)
  (tag/migration old-realm new-realm)
  (user-status/migration old-realm new-realm))