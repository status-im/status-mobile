(ns status-im.data-store.realm.schemas.account.v1.discover
  (:require [taoensso.timbre :as log]))

(def schema {:name       :discover
             :primaryKey :message-id
             :properties {:message-id   "string"
                          :name         {:type "string" :optional true}
                          :status       "string"
                          :whisper-id   "string"
                          :photo-path   {:type "string" :optional true}
                          :tags         {:type       "list"
                                         :objectType "tag"}
                          :priority     {:type "int" :default 0}
                          :last-updated "date"}})

(defn migration [old-realm new-realm]
  (log/debug "migrating discover schema"))