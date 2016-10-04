(ns status-im.data-store.realm.schemas.account.v1.pending-message
  (:require [taoensso.timbre :as log]))

(def schema {:name       :pending-message
             :primaryKey :id
             :properties {:id            :string
                          :message-id    :string
                          :chat-id       {:type     :string
                                          :optional true}
                          :ack?          :bool
                          :requires-ack? :bool
                          :from          :string
                          :to            {:type     :string
                                          :optional true}
                          :payload       :string
                          :type          :string
                          :topics        :string
                          :attempts      :int
                          :was-sent?     :bool}})

(defn migration [old-realm new-realm]
  (log/debug "migrating pending-message schema"))