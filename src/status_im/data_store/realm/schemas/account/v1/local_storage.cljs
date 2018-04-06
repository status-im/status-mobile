(ns status-im.data-store.realm.schemas.account.v1.local-storage)

(def schema {:name       :local-storage
             :primaryKey :chat-id
             :properties {:chat-id "string"
                          :data    {:type    "string"
                                    :default "{}"}}})
