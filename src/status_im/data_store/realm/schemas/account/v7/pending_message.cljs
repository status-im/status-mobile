(ns status-im.data-store.realm.schemas.account.v7.pending-message
  (:require [taoensso.timbre :as log]))

(def schema {:name       :pending-message
             :primaryKey :id
             :properties {:id            :string
                          :message-id    :string
                          :chat-id       {:type     :string
                                          :optional true}
                          :ack?          :bool
                          :requires-ack? :bool
                          :sig           :string
                          :key           :string
                          :key-type      :string
                          :to            {:type     :string
                                          :optional true}
                          :payload       :string
                          :type          :string
                          :topic         :string
                          :attempts      :int
                          :was-sent?     :bool}})

(defn migration [_ new-realm]
  (log/debug "migrating pending-message schema v7")
  (let [new-contacts (.objects new-realm "contact")]
    (dotimes [i (.-length new-contacts)]
      (.delete new-realm (aget new-contacts i)))))
