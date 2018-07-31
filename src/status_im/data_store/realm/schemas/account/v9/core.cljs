(ns status-im.data-store.realm.schemas.account.v9.core
  (:require [status-im.data-store.realm.schemas.account.v5.chat :as chat]
            [status-im.data-store.realm.schemas.account.v6.transport :as transport]
            [status-im.data-store.realm.schemas.account.v1.contact :as contact]
            [status-im.data-store.realm.schemas.account.v7.message :as message]
            [status-im.data-store.realm.schemas.account.v1.request :as request]
            [status-im.data-store.realm.schemas.account.v1.user-status :as user-status]
            [status-im.data-store.realm.schemas.account.v1.local-storage :as local-storage]
            [status-im.data-store.realm.schemas.account.v2.mailserver :as mailserver]
            [status-im.data-store.realm.schemas.account.v8.browser :as browser]
            [status-im.data-store.realm.schemas.account.v9.dapp-permissions :as dapp-permissions]
            [taoensso.timbre :as log]))

(def schema [chat/schema
             transport/schema
             contact/schema
             message/schema
             request/schema
             mailserver/schema
             user-status/schema
             local-storage/schema
             browser/schema
             dapp-permissions/schema])

(defn migration [old-realm new-realm]
  (log/debug "migrating v9 account database: " old-realm new-realm))