(ns status-im.data-store.realm.schemas.account.v1.processed-message
  (:require [taoensso.timbre :as log]))

(def schema {:name       :processed-message
             :primaryKey :id
             :properties {:id         :string
                          :message-id :string
                          :type       {:type "string"
                                       :optional true}
                          :ttl        :int}})

(defn migration [_ _]
  (log/debug "migrating processed-message schema"))
