(ns status-im.data-store.realm.schemas.account.v1.user-status
  (:require [taoensso.timbre :as log]))

(def schema {:name       :user-status
             :primaryKey :id
             :properties {:id               "string"
                          :whisper-identity {:type    "string"
                                             :default ""}
                          :status           "string"}})

(defn migration [old-realm new-realm]
  (log/debug "migrating user-status schema"))