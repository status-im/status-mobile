(ns status-im.data-store.realm.schemas.account.v2.core
  (:require [status-im.data-store.realm.schemas.account.v1.chat :as chat]
            [status-im.data-store.realm.schemas.account.v2.transport :as transport]
            [status-im.data-store.realm.schemas.account.v1.contact :as contact]
            [status-im.data-store.realm.schemas.account.v1.message :as message]
            [status-im.data-store.realm.schemas.account.v1.request :as request]
            [status-im.data-store.realm.schemas.account.v1.user-status :as user-status]
            [status-im.data-store.realm.schemas.account.v1.contact-group :as contact-group]
            [status-im.data-store.realm.schemas.account.v1.local-storage :as local-storage]
            [status-im.data-store.realm.schemas.account.v1.browser :as browser]
            [goog.object :as object]
            [taoensso.timbre :as log]
            [cljs.reader :as reader]
            [clojure.string :as string]))

(def schema [chat/schema
             transport/schema
             contact/schema
             message/schema
             request/schema
             user-status/schema
             contact-group/schema
             local-storage/schema
             browser/schema])

(defn migration [old-realm new-realm]
  (log/debug "migrating v2 account database: " old-realm new-realm))
