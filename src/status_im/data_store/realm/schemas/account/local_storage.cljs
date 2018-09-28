(ns status-im.data-store.realm.schemas.account.local-storage)

(def v1 {:name       :local-storage
         :primaryKey :chat-id
         :properties {:chat-id :string
                      :data    {:type    :string
                                :default "{}"}}})
