(ns status-im.data-store.realm.local-storage
  (:require [status-im.data-store.realm.core :as realm]))

(defn get-by-chat-id
  [chat-id]
  (realm/get-one-by-field-clj @realm/account-realm :local-storage :chat-id chat-id))

(defn save
  [local-storage]
  (realm/save @realm/account-realm :local-storage local-storage true))
