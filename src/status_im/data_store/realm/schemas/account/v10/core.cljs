(ns status-im.data-store.realm.schemas.account.v10.core
  (:require [status-im.data-store.realm.schemas.account.v5.chat :as chat]
            [status-im.data-store.realm.schemas.account.v6.transport :as transport]
            [status-im.data-store.realm.schemas.account.v1.contact :as contact]
            [status-im.data-store.realm.schemas.account.v7.message :as message]
            [status-im.data-store.realm.schemas.account.v1.user-status :as user-status]
            [status-im.data-store.realm.schemas.account.v1.local-storage :as local-storage]
            [status-im.data-store.realm.schemas.account.v2.mailserver :as mailserver]
            [status-im.data-store.realm.schemas.account.v8.browser :as browser]
            [status-im.data-store.realm.schemas.account.v9.dapp-permissions :as dapp-permissions]
            [cljs.reader :as reader]
            [taoensso.timbre :as log]))

(def schema [chat/schema
             transport/schema
             contact/schema
             message/schema
             mailserver/schema
             user-status/schema
             local-storage/schema
             browser/schema
             dapp-permissions/schema])

(defn message-by-id [realm message-id]
  (some-> realm
          (.objects "message")
          (.filtered (str "message-id = \"" message-id "\""))
          (aget 0)))

(defn migration [old-realm new-realm]
  (log/debug "migrating v10 account database")
  (some-> old-realm
          (.objects "request")
          (.filtered (str "status = \"answered\""))
          (.map (fn [request _ _]
                  (let [message-id  (aget request "message-id")
                        message     (message-by-id new-realm message-id)
                        content     (reader/read-string (aget message "content"))
                        new-content (assoc-in content [:params :answered?] true)]
                    (aset message "content" (pr-str new-content)))))))
