(ns status-im.data-store.realm.schemas.account.v17.core
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


(defn remove-chat-with-contact! [new-realm whisper-identity]
  (when-let [chat-contact (some-> new-realm
                                  (.objects "chat-contact")
                                  (.filtered (str "identity = \"" whisper-identity "\""))
                                  (aget 0))]
    (log/debug "v17 Removing chat-contact with contact" (pr-str chat-contact))
    (.delete new-realm chat-contact))
  (when-let [chat (some-> new-realm
                          (.objects "chat")
                          (.filtered (str "chat-id = \"" whisper-identity "\""))
                          (aget 0))]
    (log/debug "v17 Removing chat with contact" (pr-str chat))
    (.delete new-realm chat)))

(defn remove-contact! [new-realm whisper-identity]
  (when-let [contact (some-> new-realm
                             (.objects "contact")
                             (.filtered (str "whisper-identity = \"" whisper-identity "\""))
                             (aget 0))]
    (log/debug "v17 Removing contact" (pr-str contact))
    (.delete new-realm contact)))

(defn command-with-wrong-bot? [bot command]
  (and
   (#{"transactor-personal" "transactor-group"} bot)
   (not (#{"send" "request"} command))))

(defn update-commands [new-realm content-type]
  (some-> new-realm
          (.objects "message")
          (.filtered (str "content-type = \"" content-type "\""))
          (.map (fn [object _ _]
                  (let [{:keys [bot command] :as content} (reader/read-string (aget object "content"))
                        content' (cond->
                                  content

                                  (= "password" command)
                                  (update :params dissoc :password :password-confirmation)

                                  (command-with-wrong-bot? bot command)
                                  (assoc :bot nil))]
                    (aset object "content" (pr-str content')))))))

;; NOTE(oskarth): Resets Realm for some dApps to be loaded by default_contacts.json instead.
(defn migration [old-realm new-realm]
  (log/debug "migrating v17 account database: " old-realm new-realm)
  (doseq [contact-id ["oaken-water-meter" "gnosis" "Commiteth" "melonport" "Etherplay"]]
    (remove-chat-with-contact! new-realm contact-id)
    (remove-contact! new-realm contact-id))
  (update-commands new-realm "command")
  (update-commands new-realm "command-request"))
