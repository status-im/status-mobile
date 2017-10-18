(ns status-im.data-store.realm.schemas.account.v1.command
  (:require [taoensso.timbre :as log]))

(def schema {:name       :command
             :primaryKey :chat-id
             :properties {:chat-id "string"
                          :file    "string"}})

(defn migration [_ _]
  (log/debug "migrating command schema"))
