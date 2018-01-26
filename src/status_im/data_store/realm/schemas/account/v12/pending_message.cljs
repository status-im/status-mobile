(ns status-im.data-store.realm.schemas.account.v12.pending-message
  (:require [goog.object :as object]
            [taoensso.timbre :as log]))

(def schema {:name       :pending-message
             :primaryKey :id
             :properties {:id            :string
                          :message-id    :string
                          :chat-id       {:type     :string
                                          :optional true}
                          :ack?          :bool
                          :requires-ack? :bool
                          :sig           :string
                          :pub-key       {:type     :string
                                          :optional true}
                          :sym-key-id    {:type     :string
                                          :optional true}
                          :to            {:type     :string
                                          :optional true}
                          :payload       :string
                          :type          :string
                          :topic         :string
                          :attempts      :int
                          :was-sent?     :bool}})

(defn migration [old-realm new-realm]
  (log/debug "migrating pending-message schema v12")
  (let [messages     (.objects old-realm "pending-message")
        new-messages (.objects new-realm "pending-message")]
    (dotimes [i (.-length messages)]
      (let [message     (aget messages i)
            new-message (aget new-messages i)
            key-type    (object/get message "key-type")
            key         (object/get message "key")]
        (if (= key-type "sym")
          (aset new-message "sym-key-id" key)
          (aset new-message "pub-key" key))))))
