(ns status-im.data-store.realm.schemas.account.v21.core
  (:require [status-im.data-store.realm.schemas.account.v21.chat :as chat]
            [status-im.data-store.realm.schemas.account.v1.chat-contact :as chat-contact]
            [status-im.data-store.realm.schemas.account.v19.contact :as contact]
            [status-im.data-store.realm.schemas.account.v20.discover :as discover]
            [status-im.data-store.realm.schemas.account.v19.message :as message]
            [status-im.data-store.realm.schemas.account.v12.pending-message :as pending-message]
            [status-im.data-store.realm.schemas.account.v1.processed-message :as processed-message]
            [status-im.data-store.realm.schemas.account.v19.request :as request]
            [status-im.data-store.realm.schemas.account.v19.user-status :as user-status]
            [status-im.data-store.realm.schemas.account.v5.contact-group :as contact-group]
            [status-im.data-store.realm.schemas.account.v5.group-contact :as group-contact]
            [status-im.data-store.realm.schemas.account.v8.local-storage :as local-storage]
            [status-im.data-store.realm.schemas.account.v21.browser :as browser]
            [taoensso.timbre :as log]
            [cljs.reader :as reader]
            [clojure.string :as str]))

(def schema [chat/schema
             chat-contact/schema
             contact/schema
             discover/schema
             message/schema
             pending-message/schema
             processed-message/schema
             request/schema
             user-status/schema
             contact-group/schema
             group-contact/schema
             local-storage/schema
             browser/schema])

(defn remove-contact! [new-realm whisper-identity]
  (when-let [contact (some-> new-realm
                             (.objects "contact")
                             (.filtered (str "whisper-identity = \"" whisper-identity "\""))
                             (aget 0))]
    (log/debug "v21 Removing contact " (pr-str contact))
    (.delete new-realm contact)))

(defn remove-location-messages! [old-realm new-realm]
  (let [messages (.objects new-realm "message")]
    (dotimes [i (.-length messages)]
      (let [message (aget messages i)
            content (aget message "content")
            type    (aget message "content-type")]
        (when (and (= type "command")
                   (> (str/index-of content "command=location") -1))
          (aset message "show?" false))))))

(defn remove-phone-messages! [old-realm new-realm]
  (let [messages (.objects new-realm "message")]
    (dotimes [i (.-length messages)]
      (let [message (aget messages i)
            content (aget message "content")
            type    (aget message "content-type")]
        (when (and (= type "command")
                   (> (str/index-of content "command=phone") -1))
          (aset message "show?" false))))))

(defn chat-by-id [chats chat-id]
  (some-> chats
          (.filtered (str "chat-id = \"" chat-id "\""))
          (aget 0)))

(defn contact-by-id [contacts contact-id]
  (some-> contacts
          (.filtered (str "whisper-identity = \"" contact-id "\""))
          (aget 0)))

(defn messages-by-chat-id [messages chat-id]
  (some-> messages
          (.filtered (str "chat-id = \"" chat-id "\""))))

(defn remove-dapp! [realm dapp]
  (let [contacts (.objects realm "contact")
        chats    (.objects realm "chat")
        messages (.objects realm "message")]
    (when-let [contact (contact-by-id contacts dapp)]
      (.delete realm contact)
      (when-let [chat (chat-by-id chats dapp)]
        (.delete realm chat)
        (when-let [messages (messages-by-chat-id messages dapp)]
          (.delete realm messages))))))

(defn clean-dapps! [realm]
  (let [dapps #{"bchat" "Dentacoin" "gnosis" "melonport" "oaken-water-meter"
                "Ethcro" "Augur" "mkr-market"}]
    (doseq [dapp dapps]
      (remove-dapp! realm dapp))))

(defn migration [old-realm new-realm]
  (log/debug "migrating v21 account database: " old-realm new-realm)
  (remove-contact! new-realm "browse")
  (remove-location-messages! old-realm new-realm)
  (remove-phone-messages! old-realm new-realm)
  (clean-dapps! new-realm))
