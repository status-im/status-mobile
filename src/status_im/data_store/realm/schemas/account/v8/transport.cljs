(ns status-im.data-store.realm.schemas.account.v8.transport
  (:require
   [clojure.string :as string]
   [taoensso.timbre :as log]))

(def schema {:name       :transport
             :primaryKey :chat-id
             :properties {:chat-id               :string
                          :ack                   :string
                          :seen                  :string
                          :pending-ack           :string
                          :pending-send          :string
                          :topic                 :string
                          :one-to-one            {:type    :bool
                                                  :optional true}
                          :fetch-history?        {:type    :bool
                                                  :default false}
                          :resend?               {:type     :string
                                                  :optional true}
                          :sym-key-id            {:type    :string
                                                  :optional true}
                          ;;TODO (yenda) remove once go implements persistence
                          :sym-key               {:type     :string
                                                  :optional true}}})

(defn one-to-one? [chat-id]
  (re-matches #"^0x[0-9a-fA-F]+$" chat-id))

(defn migration [old-realm new-realm]
  (log/debug "migrating transport chats")
  (let [old-chats (.objects old-realm "transport")
        new-chats (.objects new-realm "transport")]
    (dotimes [i (.-length old-chats)]
      (let [old-chat (aget old-chats i)
            new-chat (aget new-chats i)
            chat-id  (aget old-chat "chat-id")]
        (when (one-to-one? chat-id)
          (aset new-chat "one-to-one" true))))))
